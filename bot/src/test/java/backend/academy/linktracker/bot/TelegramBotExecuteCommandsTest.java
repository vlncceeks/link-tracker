package backend.academy.linktracker.bot;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.bot.application.client.ScrapperClient;
import backend.academy.linktracker.bot.application.command.impl.ListCommand;
import backend.academy.linktracker.bot.application.dto.request.AddLinkRequest;
import backend.academy.linktracker.bot.application.dto.response.LinkResponse;
import backend.academy.linktracker.bot.application.dto.response.ListLinksResponse;
import backend.academy.linktracker.bot.application.exception.ScrapperClientException;
import backend.academy.linktracker.bot.application.state.TrackDialogHandler;
import backend.academy.linktracker.bot.application.state.TrackSession;
import backend.academy.linktracker.bot.application.state.TrackSessionRepository;
import backend.academy.linktracker.bot.application.state.TrackState;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TelegramBotExecuteCommandsTest {
    @Mock
    private ScrapperClient scrapperClient;

    @Mock
    private TrackSessionRepository sessionRepository;

    private ListCommand listCommand;
    private TrackDialogHandler handler;

    private static final Long CHAT_ID = 123L;
    private static final String VALID_GITHUB_URL = "https://github.com/user/repo";
    private static final String VALID_SO_URL = "https://stackoverflow.com/questions/12345/title";
    private static final String INVALID_URL = "tbank://github.com/user/repo";

    @BeforeEach
    void setUp() {
        listCommand = new ListCommand(scrapperClient);
        handler = new TrackDialogHandler(sessionRepository, scrapperClient);
    }

    @Test
    void handleUrl_validGithubUrl_movesToTagsStep() {
        TrackSession session = new TrackSession();
        session.setState(TrackState.WAITING_FOR_URL);
        when(sessionRepository.find(CHAT_ID)).thenReturn(Optional.of(session));

        String response = handler.handle(CHAT_ID, VALID_GITHUB_URL);

        Assertions.assertThat(response).contains("/skip");
        Assertions.assertThat(session.getUrl()).isEqualTo(VALID_GITHUB_URL);
        Assertions.assertThat(session.getState()).isEqualTo(TrackState.WAITING_FOR_TAGS);
        verifyNoInteractions(scrapperClient);
    }

    @Test
    void handleUrl_validStackOverflowUrl_movesToTagsStep() {
        TrackSession session = new TrackSession();
        session.setState(TrackState.WAITING_FOR_URL);
        when(sessionRepository.find(CHAT_ID)).thenReturn(Optional.of(session));

        String response = handler.handle(CHAT_ID, VALID_SO_URL);

        Assertions.assertThat(response).contains("/skip");
        Assertions.assertThat(session.getUrl()).isEqualTo(VALID_SO_URL);
        verifyNoInteractions(scrapperClient);
    }

    @Test
    void handleTags_withTags_callsScrapperAndReturnsSuccess() {
        TrackSession session = new TrackSession();
        session.setState(TrackState.WAITING_FOR_TAGS);
        session.setUrl(VALID_GITHUB_URL);
        when(sessionRepository.find(CHAT_ID)).thenReturn(Optional.of(session));

        String response = handler.handle(CHAT_ID, "java, spring");

        Assertions.assertThat(response).contains(VALID_GITHUB_URL);
        Assertions.assertThat(response).contains("java");
        verify(scrapperClient)
                .addLink(eq(CHAT_ID), eq(new AddLinkRequest(VALID_GITHUB_URL, List.of("java", "spring"), List.of())));
        verify(sessionRepository).delete(CHAT_ID);
    }

    @Test
    void handleTags_skipTags_callsScrapperWithEmptyTags() {
        TrackSession session = new TrackSession();
        session.setState(TrackState.WAITING_FOR_TAGS);
        session.setUrl(VALID_GITHUB_URL);
        when(sessionRepository.find(CHAT_ID)).thenReturn(Optional.of(session));

        String response = handler.handle(CHAT_ID, "/skip");

        Assertions.assertThat(response).contains(VALID_GITHUB_URL);
        verify(scrapperClient).addLink(eq(CHAT_ID), eq(new AddLinkRequest(VALID_GITHUB_URL, List.of(), List.of())));
        verify(sessionRepository).delete(CHAT_ID);
    }

    @Test
    void handleUrl_invalidUrl_returnsErrorAndStaysOnUrlStep() {
        TrackSession session = new TrackSession();
        session.setState(TrackState.WAITING_FOR_URL);
        when(sessionRepository.find(CHAT_ID)).thenReturn(Optional.of(session));

        String response = handler.handle(CHAT_ID, INVALID_URL);

        Assertions.assertThat(response).containsIgnoringCase("некорректный");
        Assertions.assertThat(session.getState()).isEqualTo(TrackState.WAITING_FOR_URL);
        Assertions.assertThat(session.getUrl()).isNull();
        verifyNoInteractions(scrapperClient);
    }

    @Test
    void handleTags_linkAlreadyTracked_returnsErrorMessage() {
        TrackSession session = new TrackSession();
        session.setState(TrackState.WAITING_FOR_TAGS);
        session.setUrl(VALID_GITHUB_URL);
        when(sessionRepository.find(CHAT_ID)).thenReturn(Optional.of(session));
        doThrow(new ScrapperClientException("Ссылка уже отслеживается"))
                .when(scrapperClient)
                .addLink(any(), any());

        String response = handler.handle(CHAT_ID, "/skip");

        Assertions.assertThat(response).containsIgnoringCase("ошибка");
        verify(sessionRepository).delete(CHAT_ID);
    }

    @Test
    void handle_cancelCommand_deletesSessionAndReturnsMessage() {
        TrackSession session = new TrackSession();
        session.setState(TrackState.WAITING_FOR_URL);
        when(sessionRepository.find(CHAT_ID)).thenReturn(Optional.of(session));

        String response = handler.handle(CHAT_ID, "/cancel");

        Assertions.assertThat(response).containsIgnoringCase("отмен");
        verify(sessionRepository).delete(CHAT_ID);
        verifyNoInteractions(scrapperClient);
    }

    @Test
    void execute_withLinks_returnsLinkList() {
        List<LinkResponse> links = List.of(
                new LinkResponse(1, VALID_GITHUB_URL, List.of(), List.of()),
                new LinkResponse(2, VALID_SO_URL, List.of(), List.of()));
        when(scrapperClient.getLinks(CHAT_ID)).thenReturn(new ListLinksResponse(links, links.size()));

        String response = listCommand.execute("user", CHAT_ID, new String[] {});

        assertThat(response).contains(VALID_GITHUB_URL);
        assertThat(response).contains(VALID_SO_URL);
    }
}
