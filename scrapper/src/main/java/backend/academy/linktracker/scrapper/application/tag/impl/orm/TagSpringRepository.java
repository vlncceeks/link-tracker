package backend.academy.linktracker.scrapper.application.tag.impl.orm;

import backend.academy.linktracker.scrapper.application.tag.Tag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface TagSpringRepository extends CrudRepository<Tag, Integer> {
    Optional<Tag> findByName(String name);

    List<Tag> findAll();
}
