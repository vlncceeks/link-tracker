package backend.academy.linktracker.scrapper.application.service.impl;

import backend.academy.linktracker.scrapper.application.client.GitHubClient;
import backend.academy.linktracker.scrapper.application.client.impl.GitHubClientImpl;
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
public class GitHubLinkUpdateChecker implements LinkUpdateChecker {
    private static final Logger logger = LoggerFactory.getLogger(GitHubLinkUpdateChecker.class);

    private final GitHubClient gitHubClient;

    @Override
    public boolean supports(String url) {
        return url.startsWith("https://github.com/");
    }

    @Override
    public Optional<String> check(TrackedLink link) {
        return GitHubClientImpl.parseUrl(link.getUrl())
                .flatMap(parts -> gitHubClient.fetchRepository(parts[0], parts[1]))
                .flatMap(response -> {
                    Instant remoteUpdatedAt = response.pushedAt();

                    logger.atDebug()
                            .addKeyValue("url", link.getUrl())
                            .addKeyValue("pushedAt", remoteUpdatedAt)
                            .addKeyValue("lastCheckedAt", link.getLastCheckedAt())
                            .log("Проверка GitHub репозитория");

                    if (remoteUpdatedAt.isAfter(link.getLastCheckedAt())) {
                        link.setLastCheckedAt(Instant.now());
                        return Optional.of("Новый коммит в репозитории " + link.getUrl());
                    }
                    return Optional.empty();
                });
    }
}
