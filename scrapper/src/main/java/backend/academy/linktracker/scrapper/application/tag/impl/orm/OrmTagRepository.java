package backend.academy.linktracker.scrapper.application.tag.impl.orm;

import backend.academy.linktracker.scrapper.application.tag.Tag;
import backend.academy.linktracker.scrapper.application.tag.TagRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrmTagRepository implements TagRepository {
    private final TagSpringRepository tagSpringRepository;

    @Override
    public Tag add(String name) {
        return tagSpringRepository.save(new Tag(null, name));
    }

    @Override
    public Tag update(String name, Tag tag) {
        return tagSpringRepository.save(new Tag(tag.id(), name));
    }

    @Override
    public Optional<Tag> findById(Integer id) {
        return tagSpringRepository.findById(id);
    }

    @Override
    public Optional<Tag> findByName(String name) {
        return tagSpringRepository.findByName(name);
    }

    @Override
    public List<Tag> findAll() {
        return tagSpringRepository.findAll();
    }

    @Override
    public void deleteById(Integer id) {
        tagSpringRepository.deleteById(id);
    }
}
