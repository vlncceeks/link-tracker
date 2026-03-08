package backend.academy.linktracker.scrapper.properties.application.chat.impl;

import backend.academy.linktracker.scrapper.properties.application.chat.ChatRepository;
import backend.academy.linktracker.scrapper.properties.application.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.properties.application.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.properties.application.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.properties.application.link.TrackedLink;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class InMemoryChatRepository implements ChatRepository {
    private final Map<Long, Map<String, TrackedLink>> storage = new HashMap<>();
    private final AtomicInteger idCounter = new AtomicInteger(1);

    @Override
    public void register(Long chatId) {
        if (storage.containsKey(chatId)) throw new ChatAlreadyExistsException(chatId);
        storage.put(chatId, new HashMap<>());
    }

    @Override
    public void delete(Long chatId) {
        if (!storage.containsKey(chatId)) throw new ChatNotFoundException(chatId);
        storage.remove(chatId);
    }

    @Override
    public boolean exists(Long chatId) {
        return storage.containsKey(chatId);
    }

    @Override
    public void addLink(Long chatId, TrackedLink link) {
        if (!storage.containsKey(chatId)) throw new ChatNotFoundException(chatId);
        getChat(chatId).put(link.getUrl(), link);
    }

    @Override
    public void removeLink(Long chatId, String url) {
        Map<String, TrackedLink> links = getChat(chatId);
        if (!links.containsKey(url)) throw new LinkNotFoundException(url);
        links.remove(url);
    }

    @Override
    public Optional<TrackedLink> findLink(Long chatId, String url) {
        return Optional.ofNullable(getChat(chatId).get(url));
    }

    @Override
    public List<TrackedLink> getLinks(Long chatId) {
        return new ArrayList<>(getChat(chatId).values());
    }

    @Override
    public Map<String, List<Long>> getAllLinksWithChats() {
        Map<String, List<Long>> result = new HashMap<>();
        storage.forEach((chatId, links) ->
            links.keySet().forEach(url ->
                result.computeIfAbsent(url, k -> new ArrayList<>()).add(chatId)
            )
        );
        return result;
    }

    public Integer nextId() {
        return idCounter.getAndIncrement();
    }

    private Map<String, TrackedLink> getChat(Long chatId) {
        if (!storage.containsKey(chatId)) throw new ChatNotFoundException(chatId);
        return storage.get(chatId);
    }
}
