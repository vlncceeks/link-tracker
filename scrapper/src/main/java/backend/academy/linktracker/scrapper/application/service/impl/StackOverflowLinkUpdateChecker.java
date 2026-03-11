package backend.academy.linktracker.scrapper.application.service.impl;

import backend.academy.linktracker.scrapper.application.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.application.client.impl.StackOverflowClientImpl;
import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import backend.academy.linktracker.scrapper.application.service.LinkUpdateChecker;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StackOverflowLinkUpdateChecker implements LinkUpdateChecker {

    private static final Logger logger = LoggerFactory.getLogger(StackOverflowLinkUpdateChecker.class);

    private final StackOverflowClient stackOverflowClient;

    @Override
    public boolean supports(String url) {
        return url.startsWith("https://stackoverflow.com/questions/");
    }

    @Override
    public Optional<String> check(TrackedLink link) {
        return StackOverflowClientImpl.parseUrl(link.getUrl())
                .flatMap(stackOverflowClient::fetchQuestion)
                .flatMap(item -> {
                    Instant remoteUpdatedAt = Instant.ofEpochSecond(item.lastActivityDate());

                    logger.atDebug()
                            .addKeyValue("url", link.getUrl())
                            .addKeyValue("lastActivityDate", remoteUpdatedAt)
                            .addKeyValue("lastCheckedAt", link.getLastCheckedAt())
                            .log("Проверка StackOverflow вопроса");

                    if (remoteUpdatedAt.isAfter(link.getLastCheckedAt())) {
                        link.setLastCheckedAt(Instant.now());
                        return Optional.of("Новая активность по вопросу " + link.getUrl());
                    }
                    return Optional.empty();
                });
    }
}
