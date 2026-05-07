package backend.academy.linktracker.scrapper;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.application.client.GitHubClient;
import backend.academy.linktracker.scrapper.application.dto.GitHubActor;
import backend.academy.linktracker.scrapper.application.dto.GitHubEventPayload;
import backend.academy.linktracker.scrapper.application.dto.GitHubIssue;
import backend.academy.linktracker.scrapper.application.dto.response.GitHubEventResponse;
import backend.academy.linktracker.scrapper.application.link.LinkRepository;
import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import backend.academy.linktracker.scrapper.infrastructure.service.impl.GitHubLinkUpdateChecker;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GitHubLinkUpdateCheckerTest {

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private GitHubClient gitHubClient;

    private GitHubLinkUpdateChecker checker;

    @BeforeEach
    void setUp() {
        checker = new GitHubLinkUpdateChecker(linkRepository, gitHubClient);
    }

    @Test
    void githubIssueEvent_formatsMessageWithTitleAuthorAndPreview() {
        GitHubEventResponse event = new GitHubEventResponse(
                "123",
                "IssuesEvent",
                new GitHubActor("octocat"),
                new GitHubEventPayload(
                        "opened",
                        null,
                        new GitHubIssue(42L, "Bug in production", "open", "https://github.com/user/repo/issues/42"),
                        "",
                        "",
                        "",
                        null),
                Instant.parse("2026-04-06T10:00:00Z"));

        TrackedLink link = new TrackedLink(1, 1L, "https://github.com/user/repo");
        link.setLastCheckedAt(Instant.parse("2026-04-06T09:00:00Z"));

        when(gitHubClient.fetchEvents("user", "repo", link.getLastCheckedAt())).thenReturn(List.of(event));

        Optional<String> result = checker.check(link);

        assertTrue(result.isPresent());
        String message = result.get();
        assertTrue(message.contains("Issue #42"), "Должен содержать номер Issue");
        assertTrue(message.contains("octocat"), "Должен содержать имя автора");
        assertTrue(message.contains("Bug in production"), "Должен содержать превью");
    }
}
