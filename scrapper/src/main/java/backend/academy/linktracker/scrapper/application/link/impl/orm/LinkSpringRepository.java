package backend.academy.linktracker.scrapper.application.link.impl.orm;

import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface LinkSpringRepository extends CrudRepository<TrackedLink, Integer> {
    Optional<TrackedLink> findByChatIdAndUrl(Long chatId, String url);

    List<TrackedLink> findAllByChatId(Long chatId);

    void deleteByChatIdAndUrl(Long chatId, String url);
}
