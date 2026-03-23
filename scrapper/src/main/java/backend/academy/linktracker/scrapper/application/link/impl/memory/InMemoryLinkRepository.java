package backend.academy.linktracker.scrapper.application.link.impl.memory;

import backend.academy.linktracker.scrapper.application.Clearable;
import backend.academy.linktracker.scrapper.application.chat.ChatRepository;
import backend.academy.linktracker.scrapper.application.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.application.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.application.link.LinkRepository;
import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryLinkRepository implements LinkRepository, Clearable {
    private final Map<Long, Map<String, TrackedLink>> storage = new HashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);
    private final ChatRepository chatRepository;

    public InMemoryLinkRepository(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    @Override
    public TrackedLink add(Long chatId, TrackedLink link) {
        if (!chatRepository.exists(chatId)) throw new ChatNotFoundException(chatId);
        storage.computeIfAbsent(chatId, k -> new HashMap<>());
        TrackedLink withId = new TrackedLink(idCounter.getAndIncrement(), chatId, link.getUrl());
        storage.get(chatId).put(withId.getUrl(), withId);
        return withId;
    }

    @Override
    public void remove(Long chatId, String url) {
        Map<String, TrackedLink> links = getLinksForChat(chatId);
        if (!links.containsKey(url)) throw new LinkNotFoundException(url);
        links.remove(url);
    }

    @Override
    public Optional<TrackedLink> find(Long chatId, String url) {
        if (!storage.containsKey(chatId)) return Optional.empty();
        return Optional.ofNullable(storage.get(chatId).get(url));
    }

    @Override
    public List<TrackedLink> findAll(Long chatId) {
        if (!storage.containsKey(chatId)) return List.of();
        return new ArrayList<>(storage.get(chatId).values());
    }

    @Override
    public Map<String, List<Long>> getAllLinksWithChats() {
        Map<String, List<Long>> result = new HashMap<>();
        storage.forEach(
                (chatId, links) -> links.keySet().forEach(url -> result.computeIfAbsent(url, k -> new ArrayList<>())
                        .add(chatId)));
        return result;
    }

    public void clear() {
        storage.clear();
        idCounter.set(1);
    }

    private Map<String, TrackedLink> getLinksForChat(Long chatId) {
        if (!storage.containsKey(chatId)) throw new ChatNotFoundException(chatId);
        return storage.get(chatId);
    }
}
