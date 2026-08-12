package backend.academy.linktracker.scrapper.application.linktag;

import java.util.List;

public interface LinkTagRepository {
    void addTagToLink(Integer linkId, Integer tagId);

    void removeTagFromLink(Integer linkId, Integer tagId);

    List<Integer> findTagIdsByLinkId(Integer linkId, int limit, long lastId);

    List<Integer> findLinkIdsByTagId(Integer tagId);

    void removeAllTagsFromLink(Integer linkId);
}
