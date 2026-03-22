package backend.academy.linktracker.scrapper.infrastructure.scheduler;

import backend.academy.linktracker.scrapper.application.client.BotClient;
import backend.academy.linktracker.scrapper.application.dto.request.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.application.link.LinkRepository;
import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import backend.academy.linktracker.scrapper.application.service.LinkUpdateChecker;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final BotClient botClient;
    private final List<LinkUpdateChecker> checkers;

    @Scheduled(fixedDelayString = "${scheduler.interval}")
    public void checkUpdates() {
        Map<String, List<Long>> allLinks = linkRepository.getAllLinksWithChats();

        if (allLinks.isEmpty()) {
            logger.atDebug().log("Нет ссылок для проверки");
            return;
        }

        allLinks.forEach((url, chatIds) -> {
            Long firstChatId = chatIds.getFirst();
            linkRepository
                    .find(firstChatId, url)
                    .ifPresentOrElse(
                            link -> findChecker(url)
                                    .ifPresentOrElse(
                                            checker -> processLink(checker, link, chatIds), () -> logger.atWarn()
                                                    .addKeyValue("url", url)
                                                    .log("Нет подходящего чекера для ссылки")),
                            () -> logger.atWarn().addKeyValue("url", url).log("Ссылка не найдена"));
        });

        logger.atInfo().log("Проверка обновлений завершена");
    }

    private Optional<LinkUpdateChecker> findChecker(String url) {
        return checkers.stream().filter(c -> c.supports(url)).findFirst();
    }

    private void processLink(LinkUpdateChecker checker, TrackedLink link, List<Long> chatIds) {
        try {
            checker.check(link).ifPresent(description -> {
                logger.atInfo()
                        .addKeyValue("url", link.getUrl())
                        .addKeyValue("chatCount", chatIds.size())
                        .log("Обнаружено обновление, отправляем уведомление");

                botClient.sendUpdate(new LinkUpdateRequest(link.getId(), link.getUrl(), description, chatIds));
            });
        } catch (Exception e) {
            logger.atError().addKeyValue("url", link.getUrl()).setCause(e).log("Ошибка при проверке ссылки");
        }
    }
}
