package backend.academy.linktracker.scrapper.application.linktag.impl.memory;

import backend.academy.linktracker.scrapper.application.linktag.LinkTagRepository;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InMemoryLinkTagRepository implements LinkTagRepository {
    private final Map<Integer, Set<Integer>> linkToTags = new HashMap<>();
    private final Map<Integer, Set<Integer>> tagToLinks = new HashMap<>();

    @Override
    public void addTagToLink(Integer linkId, Integer tagId) {
        linkToTags.computeIfAbsent(linkId, k -> new HashSet<>()).add(tagId);

        tagToLinks.computeIfAbsent(tagId, k -> new HashSet<>()).add(linkId);
    }

    @Override
    public void removeTagFromLink(Integer linkId, Integer tagId) {
        Set<Integer> tags = linkToTags.get(linkId);
        if (tags != null) {
            tags.remove(tagId);
            if (tags.isEmpty()) {
                linkToTags.remove(linkId);
            }
        }

        Set<Integer> links = tagToLinks.get(tagId);
        if (links != null) {
            links.remove(linkId);
            if (links.isEmpty()) {
                tagToLinks.remove(tagId);
            }
        }
    }

    @Override
    public List<Integer> findTagIdsByLinkId(Integer linkId, int limit, long lastId) {
        Set<Integer> tags = linkToTags.get(linkId);
        if (tags == null) {
            return List.of();
        }
        return new ArrayList<>(tags);
    }

    @Override
    public List<Integer> findLinkIdsByTagId(Integer tagId) {
        Set<Integer> links = tagToLinks.get(tagId);
        if (links == null) {
            return List.of();
        }
        return new ArrayList<>(links);
    }

    @Override
    public void removeAllTagsFromLink(Integer linkId) {
        Set<Integer> tags = linkToTags.remove(linkId);
        if (tags == null) {
            return;
        }

        for (Integer tagId : tags) {
            Set<Integer> links = tagToLinks.get(tagId);
            if (links != null) {
                links.remove(linkId);
                if (links.isEmpty()) {
                    tagToLinks.remove(tagId);
                }
            }
        }
    }
}
