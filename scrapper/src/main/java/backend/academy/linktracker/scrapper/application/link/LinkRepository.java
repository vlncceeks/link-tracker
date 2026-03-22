package backend.academy.linktracker.scrapper.application.link;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LinkRepository {
    TrackedLink add(Long chatId, TrackedLink link);

    void remove(Long chatId, String url);

    Optional<TrackedLink> find(Long chatId, String url);

    List<TrackedLink> findAll(Long chatId);

    Map<String, List<Long>> getAllLinksWithChats();
}
