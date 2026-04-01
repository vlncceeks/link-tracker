package backend.academy.linktracker.scrapper.application.link.impl.orm;

import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface LinkSpringRepository
        extends CrudRepository<TrackedLink, Integer>, PagingAndSortingRepository<TrackedLink, Integer> {
    Optional<TrackedLink> findByChatIdAndUrl(Long chatId, String url);

    Page<TrackedLink> findAllByChatId(Long chatId, Pageable pageable);

    void deleteByChatIdAndUrl(Long chatId, String url);

    @Query("""
        SELECT tl.id, tl.url, t.name as tag_name
        FROM tracked_links tl
        LEFT JOIN link_tags lt ON tl.id = lt.link_id
        LEFT JOIN tags t ON lt.tag_id = t.id
        WHERE tl.chat_id = :chatId AND tl.id > :lastId
        ORDER BY tl.id
        LIMIT :limit
        """)
    List<LinkWithTagRow> findAllWithTagsByChatIdKeySet(Long chatId, int lastId, int limit);

    Page<TrackedLink> findAll(Pageable pageable);
}
