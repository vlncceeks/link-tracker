package backend.academy.linktracker.scrapper.application.tag.impl.orm;

import backend.academy.linktracker.scrapper.application.tag.Tag;
import backend.academy.linktracker.scrapper.application.tag.TagRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

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
        List<Tag> tags = new ArrayList<>();
        int pageSize = 1000;
        int pageNumber = 0;

        Page<Tag> page;
        do {
            page = tagSpringRepository.findAll(PageRequest.of(pageNumber, pageSize, Sort.by("id")));
            tags.addAll(page.getContent());
            pageNumber++;
        } while (page.hasNext());
        return tags;
    }

    @Override
    public void deleteById(Integer id) {
        tagSpringRepository.deleteById(id);
    }
}
