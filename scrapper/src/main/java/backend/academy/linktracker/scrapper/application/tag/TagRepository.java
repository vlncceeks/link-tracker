package backend.academy.linktracker.scrapper.application.tag;

import java.util.List;
import java.util.Optional;

public interface TagRepository {
    Tag add(String name);

    Tag update(String name, Tag tag);

    Optional<Tag> findById(Integer id);

    Optional<Tag> findByName(String name);

    List<Tag> findAll();

    void deleteById(Integer id);
}
