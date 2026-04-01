package backend.academy.linktracker.scrapper.application.tag.impl.orm;

import backend.academy.linktracker.scrapper.application.tag.Tag;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

public interface TagSpringRepository extends CrudRepository<Tag, Integer> {
    Optional<Tag> findByName(String name);

    Page<Tag> findAll(Pageable pageable);
}
