package backend.academy.linktracker.scrapper.application.linktag.impl.orm;

import backend.academy.linktracker.scrapper.application.linktag.LinkTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface LinkTagSpringRepository extends CrudRepository<LinkTag, Integer> {
    Page<LinkTag> findAllByLinkId(Integer linkId, Pageable pageable);

    Page<LinkTag> findAllByTagId(Integer tagId, Pageable pageable);

    void deleteAllByLinkId(Integer linkId);
}
