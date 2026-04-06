package backend.academy.linktracker.scrapper.application.linktag.impl.orm;

import backend.academy.linktracker.scrapper.application.linktag.LinkTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface LinkTagSpringRepository extends CrudRepository<LinkTag, Integer> {
    List<LinkTag> findAllByLinkIdAndIdGreaterThan(Integer linkId, long lastId, Pageable pageable);

    Page<LinkTag> findAllByTagId(Integer tagId, Pageable pageable);

    void deleteAllByLinkId(Integer linkId);
}
