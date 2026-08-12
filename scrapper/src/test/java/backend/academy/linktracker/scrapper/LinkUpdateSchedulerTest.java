package backend.academy.linktracker.scrapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.scrapper.application.client.MessageSender;
import backend.academy.linktracker.scrapper.application.dto.InternalUpdateEvent;
import backend.academy.linktracker.scrapper.application.dto.request.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.application.dto.response.LinksPage;
import backend.academy.linktracker.scrapper.application.link.LinkRepository;
import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import backend.academy.linktracker.scrapper.infrastructure.configuration.SchedulerProperties;
import backend.academy.linktracker.scrapper.infrastructure.scheduler.LinkUpdateScheduler;
import backend.academy.linktracker.scrapper.infrastructure.service.LinkUpdateChecker;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private MessageSender messageSender;

    @Mock
    private LinkUpdateChecker checker;

    private SchedulerProperties properties;

    private LinkUpdateScheduler scheduler;

    private static final String URL = "https://github.com/user/repo";
    private static final Long CHAT_ID_1 = 1L;
    private static final Long CHAT_ID_2 = 2L;

    @BeforeEach
    void setUp() {
        properties = new SchedulerProperties(60, 1000, 4, false);
        scheduler = new LinkUpdateScheduler(linkRepository, messageSender, List.of(checker), properties);
        scheduler.init();
    }

    @Test
    void checkUpdates_sendsNotificationOnlyToSubscribers() {
        TrackedLink link = new TrackedLink(1, 1L, URL);
        link.setLastCheckedAt(Instant.now().minusSeconds(3600));

        LinksPage page = new LinksPage(Map.of(URL, List.of(CHAT_ID_1, CHAT_ID_2)), 1L);
        LinksPage emptyPage = new LinksPage(Map.of(), 1L);

        when(linkRepository.getLinksWithChats(1000, 0L)).thenReturn(page);
        when(linkRepository.find(CHAT_ID_1, URL)).thenReturn(Optional.of(link));
        when(checker.supports(URL)).thenReturn(true);
        when(checker.check(link)).thenReturn(Optional.of(new InternalUpdateEvent("author", "Новый коммит")));

        scheduler.checkUpdates();

        verify(messageSender, times(1)).send(any(LinkUpdateRequest.class));
        verify(messageSender)
                .send(argThat(req -> req.chatIds().containsAll(List.of(CHAT_ID_1, CHAT_ID_2))
                        && req.chatIds().size() == 2
                        && req.url().equals(URL)));
    }

    @Test
    void checkUpdates_noUpdates_doesNotSendNotification() {
        TrackedLink link = new TrackedLink(1, 1L, URL);
        link.setLastCheckedAt(Instant.now());

        LinksPage page = new LinksPage(Map.of(URL, List.of(CHAT_ID_1)), 1L);
        LinksPage emptyPage = new LinksPage(Map.of(), 1L);

        when(linkRepository.getLinksWithChats(1000, 0L)).thenReturn(page);
        when(linkRepository.find(CHAT_ID_1, URL)).thenReturn(Optional.of(link));
        when(checker.supports(URL)).thenReturn(true);
        when(checker.check(link)).thenReturn(Optional.empty());

        scheduler.checkUpdates();

        verifyNoInteractions(messageSender);
    }

    @Test
    void checkUpdates_noLinks_doesNothing() {
        LinksPage emptyPage = new LinksPage(Map.of(), 0L);
        when(linkRepository.getLinksWithChats(1000, 0L)).thenReturn(emptyPage);

        scheduler.checkUpdates();

        verifyNoInteractions(messageSender);
        verifyNoInteractions(checker);
    }

    @Test
    void checkUpdates_checkerThrows_doesNotCrash() {
        TrackedLink link = new TrackedLink(1, 1L, URL);
        link.setLastCheckedAt(Instant.now());

        LinksPage page = new LinksPage(Map.of(URL, List.of(CHAT_ID_1)), 1L);
        LinksPage emptyPage = new LinksPage(Map.of(), 1L);

        when(linkRepository.getLinksWithChats(1000, 0L)).thenReturn(page);
        when(linkRepository.find(CHAT_ID_1, URL)).thenReturn(Optional.of(link));
        when(checker.supports(URL)).thenReturn(true);
        when(checker.check(link)).thenThrow(new RuntimeException("Сеть недоступна"));

        scheduler.checkUpdates();

        verifyNoInteractions(messageSender);
    }

    @Test
    void checkUpdates_batchProcessed_allLinksHandled() {
        String url2 = "https://github.com/user/repo2";
        TrackedLink link1 = new TrackedLink(1, CHAT_ID_1, URL);
        TrackedLink link2 = new TrackedLink(2, CHAT_ID_1, url2);
        link1.setLastCheckedAt(Instant.now().minusSeconds(3600));
        link2.setLastCheckedAt(Instant.now().minusSeconds(3600));

        LinksPage page = new LinksPage(Map.of(URL, List.of(CHAT_ID_1), url2, List.of(CHAT_ID_1)), 2L);
        LinksPage emptyPage = new LinksPage(Map.of(), 2L);

        when(linkRepository.getLinksWithChats(1000, 0L)).thenReturn(page);
        when(linkRepository.find(CHAT_ID_1, URL)).thenReturn(Optional.of(link1));
        when(linkRepository.find(CHAT_ID_1, url2)).thenReturn(Optional.of(link2));
        when(checker.supports(anyString())).thenReturn(true);
        when(checker.check(any())).thenReturn(Optional.of(new InternalUpdateEvent("author", "Обновление")));

        scheduler.checkUpdates();

        verify(messageSender, times(2)).send(any(LinkUpdateRequest.class));
    }

    @Test
    void checkUpdates_oneLinkFails_othersStillProcessed() {
        String url2 = "https://github.com/user/repo2";
        TrackedLink link1 = new TrackedLink(1, CHAT_ID_1, URL);
        TrackedLink link2 = new TrackedLink(2, CHAT_ID_1, url2);
        link1.setLastCheckedAt(Instant.now().minusSeconds(3600));
        link2.setLastCheckedAt(Instant.now().minusSeconds(3600));

        LinksPage page = new LinksPage(Map.of(URL, List.of(CHAT_ID_1), url2, List.of(CHAT_ID_1)), 2L);
        LinksPage emptyPage = new LinksPage(Map.of(), 2L);

        when(linkRepository.getLinksWithChats(1000, 0L)).thenReturn(page);
        when(linkRepository.find(CHAT_ID_1, URL)).thenReturn(Optional.of(link1));
        when(linkRepository.find(CHAT_ID_1, url2)).thenReturn(Optional.of(link2));
        when(checker.supports(anyString())).thenReturn(true);
        when(checker.check(link1)).thenThrow(new RuntimeException("Сеть недоступна"));
        when(checker.check(link2)).thenReturn(Optional.of(new InternalUpdateEvent("author", "Обновление")));

        scheduler.checkUpdates();

        verify(messageSender, times(1)).send(any(LinkUpdateRequest.class));
    }
}
