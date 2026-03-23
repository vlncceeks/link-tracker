package backend.academy.linktracker.scrapper.application.linktag.impl.orm;

import backend.academy.linktracker.scrapper.application.linktag.LinkTag;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface LinkTagSpringRepository extends CrudRepository<LinkTag, Integer> {
    List<LinkTag> findAllByLinkId(Integer linkId);

    List<LinkTag> findAllByTagId(Integer tagId);

    void deleteAllByLinkId(Integer linkId);
}
