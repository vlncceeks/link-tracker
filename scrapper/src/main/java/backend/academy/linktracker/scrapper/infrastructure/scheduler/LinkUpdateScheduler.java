package backend.academy.linktracker.scrapper.infrastructure.scheduler;

import backend.academy.linktracker.scrapper.application.client.MessageSender;
import backend.academy.linktracker.scrapper.application.dto.request.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.application.dto.response.LinksPage;
import backend.academy.linktracker.scrapper.application.link.LinkRepository;
import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import backend.academy.linktracker.scrapper.infrastructure.configuration.SchedulerProperties;
import backend.academy.linktracker.scrapper.infrastructure.service.LinkUpdateChecker;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LinkUpdateScheduler {

    private static final Logger logger = LoggerFactory.getLogger(LinkUpdateScheduler.class);

    private final LinkRepository linkRepository;
    private final MessageSender messageSender;
    private final List<LinkUpdateChecker> checkers;
    private final SchedulerProperties properties;
    private volatile long lastId;
    private ExecutorService pool;

    @PostConstruct
    public void init() {
        int threadCount = properties.threadCount();
        pool = new ThreadPoolExecutor(threadCount, threadCount, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    @PreDestroy
    public void destroy() throws InterruptedException {
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
    }

    @Scheduled(fixedDelayString = "${app.scheduler.interval}")
    public void checkUpdates() {
        List<String> fails = new ArrayList<>();

        lastId = 0;
        while (true) {
            LinksPage page = linkRepository.getLinksWithChats(properties.batchSize(), lastId);
            if (page.links().isEmpty()) break;

            List<Map<String, List<Long>>> chunks = splitPageIntoChunks(page, properties.threadCount());
            List<Callable<List<String>>> tasks = chunks.stream()
                    .map(chunk -> (Callable<List<String>>) () -> processLinks(chunk))
                    .toList();

            try {
                List<Future<List<String>>> futures = pool.invokeAll(tasks);
                for (Future<List<String>> future : futures) fails.addAll(future.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.atError().log("Обработка прервана");
                break;
            } catch (ExecutionException e) {
                logger.atError().setCause(e).log("Неожиданная ошибка в потоке");
            }

            lastId = page.lastId();
            if (page.links().size() < properties.batchSize()) break;
        }

        if (!fails.isEmpty()) {
            logger.atWarn()
                    .addKeyValue("count", fails.size())
                    .addKeyValue("urls", fails)
                    .log("Не удалось обработать следующие ссылки");
        }
        logger.atInfo().log("Проверка обновлений завершена");
    }

    private Optional<LinkUpdateChecker> findChecker(String url) {
        return checkers.stream().filter(c -> c.supports(url)).findFirst();
    }

    private List<String> processLinks(Map<String, List<Long>> links) {
        List<String> failed = new ArrayList<>();

        links.forEach((url, chatIds) -> {
            if (chatIds.isEmpty()) {
                logger.atWarn().addKeyValue("url", url).log("Ссылка без чатов, пропускаем");
                return;
            }
            Long firstChatId = chatIds.getFirst();
            linkRepository
                    .find(firstChatId, url)
                    .ifPresentOrElse(
                            link -> findChecker(url)
                                    .ifPresentOrElse(
                                            checker -> checkLink(checker, link, chatIds, failed), () -> logger.atWarn()
                                                    .addKeyValue("url", url)
                                                    .log("Нет подходящего чекера для ссылки")),
                            () -> logger.atWarn().addKeyValue("url", url).log("Ссылка не найдена"));
        });

        return failed;
    }

    private void checkLink(LinkUpdateChecker checker, TrackedLink link, List<Long> chatIds, List<String> failed) {
        try {
            checker.check(link).ifPresent(description -> {
                logger.atInfo()
                        .addKeyValue("url", link.getUrl())
                        .addKeyValue("chatCount", chatIds.size())
                        .log("Обнаружено обновление, отправляем уведомление");

                messageSender.send(new LinkUpdateRequest(link.getId(), link.getUrl(), description, chatIds));
            });
        } catch (Exception e) {
            logger.atError().addKeyValue("url", link.getUrl()).setCause(e).log("Ошибка при проверке ссылки");
            failed.add(link.getUrl());
        }
    }

    private List<Map<String, List<Long>>> splitPageIntoChunks(LinksPage page, int countChunks) {
        List<Map<String, List<Long>>> allChunks = new ArrayList<>();
        Map<String, List<Long>> chunk = null;
        Map<String, List<Long>> links = page.links();

        if (countChunks < 1 || countChunks > links.size()) return List.of(links);
        int sizeChunk = links.size() / countChunks;

        int i = 0;
        for (Map.Entry<String, List<Long>> entry : links.entrySet()) {
            if (i == 0 || countChunks > 0 && i % sizeChunk == 0) {
                chunk = new HashMap<>();
                allChunks.add(chunk);
                countChunks--;
            }
            chunk.put(entry.getKey(), entry.getValue());
            i++;
        }
        return allChunks;
    }
}
