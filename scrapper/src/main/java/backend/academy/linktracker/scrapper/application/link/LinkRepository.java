package backend.academy.linktracker.scrapper.application.link;

import backend.academy.linktracker.scrapper.application.dto.response.LinkResponse;
import backend.academy.linktracker.scrapper.application.dto.response.LinksPage;
import java.util.List;
import java.util.Optional;

public interface LinkRepository {
    TrackedLink add(Long chatId, TrackedLink link);

    void remove(Long chatId, String url);

    Optional<TrackedLink> find(Long chatId, String url);

    TrackedLink update(TrackedLink link);

    LinksPage getLinksWithChats(int limit, long lastId);

    List<LinkResponse> findAllWithTags(Long chatId, int limit, long lastId);
}
