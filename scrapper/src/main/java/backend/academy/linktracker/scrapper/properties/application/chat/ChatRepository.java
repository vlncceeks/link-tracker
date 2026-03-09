package backend.academy.linktracker.scrapper.properties.application.chat;

import backend.academy.linktracker.scrapper.properties.application.link.TrackedLink;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ChatRepository {
    void register(Long chatId);

    void delete(Long chatId);

    boolean exists(Long chatId);

    void addLink(Long chatId, TrackedLink link);

    void removeLink(Long chatId, String url);

    Optional<TrackedLink> findLink(Long chatId, String url);

    List<TrackedLink> getLinks(Long chatId);

    Map<String, List<Long>> getAllLinksWithChats();

    Integer nextId();
}
