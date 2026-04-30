package backend.academy.linktracker.scrapper.application.link.impl.memory;

import backend.academy.linktracker.scrapper.application.Clearable;
import backend.academy.linktracker.scrapper.application.chat.ChatRepository;
import backend.academy.linktracker.scrapper.application.dto.response.LinkResponse;
import backend.academy.linktracker.scrapper.application.dto.response.LinksPage;
import backend.academy.linktracker.scrapper.application.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.application.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.application.link.LinkRepository;
import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
public class InMemoryLinkRepository implements LinkRepository, Clearable {
    private final Map<Long, Map<String, TrackedLink>> storage = new HashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);
    private final Map<Integer, List<String>> linkTags = new HashMap<>();

    @Override
    public TrackedLink add(Long chatId, TrackedLink link) {
        storage.computeIfAbsent(chatId, k -> new HashMap<>());
        TrackedLink withId = new TrackedLink(idCounter.getAndIncrement(), chatId, link.getUrl());
        storage.get(chatId).put(withId.getUrl(), withId);
        return withId;
    }

    @Override
    public void remove(Long chatId, String url) {
        Map<String, TrackedLink> links = getLinksForChat(chatId);
        links.remove(url);
    }

    @Override
    public Optional<TrackedLink> find(Long chatId, String url) {
        return ofNullable(storage.get(chatId)).map(s -> s.get(url));
    }

    @Override
    public TrackedLink update(TrackedLink link) {
        storage.get(link.getChatId()).put(link.getUrl(), link);
        return storage.get(link.getChatId()).get(link.getUrl());
    }

    @Override
    public LinksPage getLinksWithChats(int limit, long lastId) {
        Map<String, List<Long>> result = new LinkedHashMap<>();
        storage.forEach(
                (chatId, links) -> links.keySet().forEach(url -> result.computeIfAbsent(url, k -> new ArrayList<>())
                        .add(chatId)));
        return new LinksPage(result, 0);
    }

    public void clear() {
        storage.clear();
        linkTags.clear();
        idCounter.set(1);
    }

    private Map<String, TrackedLink> getLinksForChat(Long chatId) {
        return ofNullable(storage.get(chatId))
            .orElseThrow(() -> new ChatNotFoundException(chatId));
    }

    @Override
    public List<LinkResponse> findAllWithTags(Long chatId, int limit, long lastId) {
        if (!storage.containsKey(chatId)) return List.of();

        return storage.get(chatId).values().stream()
                .map(link -> new LinkResponse(
                        link.getId(), link.getUrl(), linkTags.getOrDefault(link.getId(), List.of()), List.of()))
                .toList();
    }
}
