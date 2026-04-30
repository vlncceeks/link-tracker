package backend.academy.linktracker.scrapper.application.tag.impl.memory;

import backend.academy.linktracker.scrapper.application.tag.Tag;
import backend.academy.linktracker.scrapper.application.tag.TagRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryTagRepository implements TagRepository {

    private final Map<Integer, Tag> storage = new HashMap<>();
    private final Map<String, Integer> nameIndex = new HashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @Override
    public Tag add(String name) {
        int newId = idGenerator.getAndIncrement();
        Tag newTag = new Tag(newId, name);

        storage.put(newId, newTag);
        nameIndex.put(name, newId);

        return newTag;
    }

    @Override
    public Tag update(String name, Tag tag) {
        Tag newTag = new Tag(tag.getId(), name);
        nameIndex.remove(tag.getName());
        nameIndex.put(name, tag.getId());
        storage.put(tag.getId(), newTag);
        return newTag;
    }

    @Override
    public Optional<Tag> findById(Integer id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Tag> findByName(String name) {
        Integer id = nameIndex.get(name);
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Tag> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void deleteById(Integer id) {
        Tag removed = storage.remove(id);
        if (removed != null) {
            nameIndex.remove(removed.getName());
        }
    }
}
