package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.application.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.application.dto.InternalUpdateEvent;
import backend.academy.linktracker.scrapper.application.dto.Owner;
import backend.academy.linktracker.scrapper.application.dto.response.StackOverflowAnswerResponse;
import backend.academy.linktracker.scrapper.application.dto.response.StackOverflowResponse;
import backend.academy.linktracker.scrapper.application.link.LinkRepository;
import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import backend.academy.linktracker.scrapper.infrastructure.service.impl.StackOverflowLinkUpdateChecker;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class StackOverflowLinkUpdateCheckerTest {

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private StackOverflowClient stackOverflowClient;

    private StackOverflowLinkUpdateChecker checker;

    private static final String URL = "https://stackoverflow.com/questions/12345/title";

    @BeforeEach
    void setUp() {
        checker = new StackOverflowLinkUpdateChecker(linkRepository, stackOverflowClient);
    }

    @Test
    void newAnswer_formatsMessageWithTitleAuthorAndPreview() {
        TrackedLink link = new TrackedLink(1, 1L, URL);
        link.setLastCheckedAt(Instant.parse("2026-04-06T09:00:00Z"));

        var owner = new Owner("john_doe");
        var answer = new StackOverflowAnswerResponse.AnswerItem(
                1L, owner, Instant.parse("2026-04-06T10:00:00Z"), "Это текст ответа");

        when(stackOverflowClient.fetchQuestion(12345L))
                .thenReturn(
                        Optional.of(new StackOverflowResponse.StackOverflowItem(12345L, "Как работает JVM?", null)));
        when(stackOverflowClient.fetchAnswers(12345L, link.getLastCheckedAt())).thenReturn(List.of(answer));
        when(stackOverflowClient.fetchComments(12345L, link.getLastCheckedAt())).thenReturn(List.of());

        Optional<InternalUpdateEvent> result = checker.check(link);

        assertThat(result).isPresent();
        assertThat(result.get().description()).contains("Как работает JVM?");
        assertThat(result.get().author()).contains("john_doe");
        assertThat(result.get().description()).contains("Это текст ответа");
    }

    @Test
    void apiUnavailable_returnsEmpty() {
        TrackedLink link = new TrackedLink(1, 1L, URL);
        link.setLastCheckedAt(Instant.parse("2026-04-06T09:00:00Z"));

        when(stackOverflowClient.fetchAnswers(12345L, link.getLastCheckedAt()))
                .thenThrow(new RestClientException("недоступен"));

        Optional<InternalUpdateEvent> result = checker.check(link);

        assertThat(result).isEmpty();
    }

    @Test
    void preview_longerThan200chars_isTruncated() {
        TrackedLink link = new TrackedLink(1, 1L, URL);
        link.setLastCheckedAt(Instant.parse("2026-04-06T09:00:00Z"));

        String longBody = "А".repeat(300);
        var answer = new StackOverflowAnswerResponse.AnswerItem(
                1L, new Owner("user"), Instant.parse("2026-04-06T10:00:00Z"), longBody);

        when(stackOverflowClient.fetchQuestion(12345L)).thenReturn(Optional.empty());
        when(stackOverflowClient.fetchAnswers(12345L, link.getLastCheckedAt())).thenReturn(List.of(answer));
        when(stackOverflowClient.fetchComments(12345L, link.getLastCheckedAt())).thenReturn(List.of());

        Optional<InternalUpdateEvent> result = checker.check(link);

        assertThat(result).isPresent();
        String preview = result.get()
                .description()
                .lines()
                .filter(l -> l.startsWith("Превью:"))
                .findFirst()
                .orElse("");
        assertThat(preview.replace("Превью: ", "").length()).isLessThanOrEqualTo(200);
    }
}
