package backend.academy.linktracker.scrapper.infrastructure.service.impl;

import backend.academy.linktracker.scrapper.application.client.GitHubClient;
import backend.academy.linktracker.scrapper.application.client.GitHubClientImpl.GitHubUrlParser;
import backend.academy.linktracker.scrapper.application.dto.response.GitHubEventResponse;
import backend.academy.linktracker.scrapper.application.link.LinkRepository;
import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import backend.academy.linktracker.scrapper.infrastructure.service.LinkUpdateChecker;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class GitHubLinkUpdateChecker implements LinkUpdateChecker {
    private static final Logger logger = LoggerFactory.getLogger(GitHubLinkUpdateChecker.class);
    private final LinkRepository linkRepository;
    private final GitHubClient clientWrapper;

    private static final Set<String> TRACKED_EVENT_TYPES = Set.of("PullRequestEvent", "IssuesEvent", "PushEvent");

    @Override
    public boolean supports(String url) {
        return url.startsWith("https://github.com/");
    }

    @Override
    public Optional<String> check(TrackedLink link) {
        return GitHubUrlParser.parseUrl(link.getUrl()).flatMap(parts -> {
            try {
                List<GitHubEventResponse> events =
                        clientWrapper.fetchEvents(parts[0], parts[1], link.getLastCheckedAt());
                List<GitHubEventResponse> relevant = events.stream()
                        .filter(e -> TRACKED_EVENT_TYPES.contains(e.type()))
                        .toList();

                logger.atDebug()
                        .addKeyValue("url", link.getUrl())
                        .addKeyValue("lastCheckedAt", link.getLastCheckedAt())
                        .log("Проверка GitHub репозитория");

                if (relevant.isEmpty()) return Optional.empty();

                link.setLastCheckedAt(Instant.now());
                linkRepository.update(link);

                return Optional.of(buildMessage(relevant));
            } catch (RestClientException e) {
                logger.atWarn()
                        .addKeyValue("url", link.getUrl())
                        .addKeyValue("error", e.getMessage())
                        .log("Ошибка при обращении к GitHub API");
                return Optional.empty();
            }
        });
    }

    private String buildMessage(List<GitHubEventResponse> events) {
        StringBuilder sb = new StringBuilder();

        for (GitHubEventResponse event : events) {
            switch (event.type()) {
                case "PullRequestEvent" -> {
                    var pr = event.payload().pullRequest();
                    if (pr == null) break;
                    sb.append("PR #")
                            .append(pr.number())
                            .append(" [")
                            .append(event.payload().action())
                            .append("]\n")
                            .append("Автор: ")
                            .append(event.actor().login())
                            .append("\n")
                            .append("Время: ")
                            .append(event.createdAt())
                            .append("\n")
                            .append("Ветка: ")
                            .append(pr.head().ref())
                            .append(" -> main\n\n");
                }
                case "IssuesEvent" -> {
                    var issue = event.payload().issue();
                    if (issue == null) break;
                    String preview = issue.title() != null && issue.title().length() > 200
                            ? issue.title().substring(0, 197) + "..."
                            : issue.title();
                    sb.append("Issue #")
                            .append(issue.number())
                            .append(" [")
                            .append(event.payload().action())
                            .append("]\n")
                            .append("Автор: ")
                            .append(event.actor().login())
                            .append("\n")
                            .append("Время: ")
                            .append(event.createdAt())
                            .append("\n")
                            .append("Превью: ")
                            .append(preview)
                            .append("\n\n");
                }
                case "PushEvent" -> {
                    String ref = event.payload().ref();
                    String head = event.payload().head();
                    String branch = ref != null ? ref.replace("refs/heads/", "") : "unknown";
                    sb.append("Push в репозиторий\n")
                            .append("Автор: ")
                            .append(event.actor().login())
                            .append("\n")
                            .append("Время: ")
                            .append(event.createdAt())
                            .append("\n")
                            .append("Ветка: ")
                            .append(branch)
                            .append("\n")
                            .append("Коммит: ")
                            .append(head != null ? head.substring(0, 7) : "unknown")
                            .append("\n\n");
                }
                default -> {}
            }
        }

        return sb.toString().trim();
    }
}
