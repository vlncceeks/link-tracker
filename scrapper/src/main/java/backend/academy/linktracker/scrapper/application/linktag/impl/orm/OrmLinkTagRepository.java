package backend.academy.linktracker.scrapper.application.linktag.impl.orm;

import backend.academy.linktracker.scrapper.application.linktag.LinkTag;
import backend.academy.linktracker.scrapper.application.linktag.LinkTagRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

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
    public List<Integer> findTagIdsByLinkId(Integer linkId) {
        return linkTagSpringRepository.findAllByLinkId(linkId).stream()
                .map(LinkTag::tagId)
                .collect(Collectors.toList());
    }

    @Override
    public List<Integer> findLinkIdsByTagId(Integer tagId) {
        return linkTagSpringRepository.findAllByTagId(tagId).stream()
                .map(LinkTag::linkId)
                .collect(Collectors.toList());
    }

    @Override
    public void removeAllTagsFromLink(Integer linkId) {
        linkTagSpringRepository.deleteAllByLinkId(linkId);
    }
}
