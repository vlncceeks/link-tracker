package backend.academy.linktracker.scrapper.application.linktag.impl.orm;

import backend.academy.linktracker.scrapper.application.linktag.LinkTag;
import backend.academy.linktracker.scrapper.application.linktag.LinkTagRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@RequiredArgsConstructor
public class OrmLinkTagRepository implements LinkTagRepository {
    private final LinkTagSpringRepository linkTagSpringRepository;

    @Override
    public void addTagToLink(Integer linkId, Integer tagId) {
        linkTagSpringRepository.save(new LinkTag(null, linkId, tagId));
    }

    @Override
    public void removeTagFromLink(Integer linkId, Integer tagId) {
        linkTagSpringRepository.delete(new LinkTag(null, linkId, tagId));
    }

    @Override
    public List<Integer> findTagIdsByLinkId(Integer linkId, int limit, long lastId) {
        return linkTagSpringRepository
            .findAllByLinkIdAndIdGreaterThan(linkId, lastId, PageRequest.of(0, limit, Sort.by("id")))
            .stream()
            .map(LinkTag::getTagId)
            .toList();
    }

    @Override
    public List<Integer> findLinkIdsByTagId(Integer tagId) {
        List<Integer> ids = new ArrayList<>();
        int pageSize = 1000;
        int pageNumber = 0;

        Page<LinkTag> page;
        do {
            page = linkTagSpringRepository.findAllByTagId(tagId, PageRequest.of(pageNumber, pageSize, Sort.by("id")));
            page.getContent().forEach(p -> ids.add(p.getLinkId()));
            pageNumber++;
        } while (page.hasNext());
        return ids;
    }

    @Override
    public void removeAllTagsFromLink(Integer linkId) {
        linkTagSpringRepository.deleteAllByLinkId(linkId);
    }
}
