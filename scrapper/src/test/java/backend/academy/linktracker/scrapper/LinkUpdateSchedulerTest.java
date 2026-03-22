package backend.academy.linktracker.scrapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.application.client.BotClient;
import backend.academy.linktracker.scrapper.application.dto.request.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.application.link.LinkRepository;
import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import backend.academy.linktracker.scrapper.application.service.LinkUpdateChecker;
import backend.academy.linktracker.scrapper.infrastructure.scheduler.LinkUpdateScheduler;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LinkUpdateSchedulerTest {

    @Mock
    private LinkRepository linkRepository;

    @Mock
    private BotClient botClient;

    @Mock
    private LinkUpdateChecker checker;

    private LinkUpdateScheduler scheduler;

    private static final String URL = "https://github.com/user/repo";
    private static final Long CHAT_ID_1 = 1L;
    private static final Long CHAT_ID_2 = 2L;

    @BeforeEach
    void setUp() {
        scheduler = new LinkUpdateScheduler(linkRepository, botClient, List.of(checker));
    }

    @Test
    void checkUpdates_sendsNotificationOnlyToSubscribers() {
        TrackedLink link = new TrackedLink(1, 1l, URL, Set.of(), Set.of());
        link.setLastCheckedAt(Instant.now().minusSeconds(3600));

        when(linkRepository.getAllLinksWithChats()).thenReturn(Map.of(URL, List.of(CHAT_ID_1, CHAT_ID_2)));
        when(linkRepository.find(CHAT_ID_1, URL)).thenReturn(Optional.of(link));
        when(checker.supports(URL)).thenReturn(true);
        when(checker.check(link)).thenReturn(Optional.of("Новый коммит"));

        scheduler.checkUpdates();

        verify(botClient, times(1)).sendUpdate(any(LinkUpdateRequest.class));
        verify(botClient).sendUpdate(argThat(req ->
            req.tgChatIds().containsAll(List.of(CHAT_ID_1, CHAT_ID_2))
                && req.tgChatIds().size() == 2
                && req.url().equals(URL)));
    }

    @Test
    void checkUpdates_noUpdates_doesNotSendNotification() {
        TrackedLink link = new TrackedLink(1, 1l, URL, Set.of(), Set.of());
        link.setLastCheckedAt(Instant.now());

        when(linkRepository.getAllLinksWithChats()).thenReturn(Map.of(URL, List.of(CHAT_ID_1)));
        when(linkRepository.find(CHAT_ID_1, URL)).thenReturn(Optional.of(link));
        when(checker.supports(URL)).thenReturn(true);
        when(checker.check(link)).thenReturn(Optional.empty());

        scheduler.checkUpdates();

        verifyNoInteractions(botClient);
    }

    @Test
    void checkUpdates_noLinks_doesNothing() {
        when(linkRepository.getAllLinksWithChats()).thenReturn(Map.of());

        scheduler.checkUpdates();

        verifyNoInteractions(botClient);
        verifyNoInteractions(checker);
    }

    @Test
    void checkUpdates_checkerThrows_doesNotCrash() {
        TrackedLink link = new TrackedLink(1,1l, URL, Set.of(), Set.of());
        link.setLastCheckedAt(Instant.now());

        when(linkRepository.getAllLinksWithChats()).thenReturn(Map.of(URL, List.of(CHAT_ID_1)));
        when(linkRepository.find(CHAT_ID_1, URL)).thenReturn(Optional.of(link));
        when(checker.supports(URL)).thenReturn(true);
        when(checker.check(link)).thenThrow(new RuntimeException("Сеть недоступна"));

        scheduler.checkUpdates();

        verifyNoInteractions(botClient);
    }
}
