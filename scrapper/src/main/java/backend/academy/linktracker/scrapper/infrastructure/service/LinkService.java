package backend.academy.linktracker.scrapper.infrastructure.service;

import backend.academy.linktracker.scrapper.application.dto.request.AddLinkRequest;
import backend.academy.linktracker.scrapper.application.dto.request.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.application.dto.response.LinkResponse;
import backend.academy.linktracker.scrapper.application.dto.response.ListLinksResponse;
import backend.academy.linktracker.scrapper.application.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.application.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.application.link.LinkRepository;
import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import backend.academy.linktracker.scrapper.application.linktag.LinkTagRepository;
import backend.academy.linktracker.scrapper.application.tag.Tag;
import backend.academy.linktracker.scrapper.application.tag.TagRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LinkService {
    private static final Logger logger = LoggerFactory.getLogger(LinkService.class);
    private final LinkRepository linkRepository;
    private final TagRepository tagRepository;
    private final LinkTagRepository linkTagRepository;

    public ListLinksResponse getAllByChatId(Long chatId) {
        List<LinkResponse> links = linkRepository.findAllWithTags(chatId);

        logger.atDebug()
                .addKeyValue("chatId", chatId)
                .addKeyValue("count", links.size())
                .log("Receive list of links");

        return new ListLinksResponse(links, links.size());
    }

    @Transactional
    public LinkResponse addLinkIntoChat(Long chatId, AddLinkRequest request) {
        if (linkRepository.find(chatId, request.url()).isPresent()) {
            logger.atWarn()
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("url", request.url())
                    .log("Link already tracked");
            throw new LinkAlreadyTrackedException(request.url());
        }

        TrackedLink link = linkRepository.add(chatId, new TrackedLink(null, chatId, request.url()));

        List<String> tagNames = new ArrayList<>();
        for (String tagName : request.tags()) {
            Tag tag = tagRepository.findByName(tagName).orElseGet(() -> tagRepository.add(tagName));
            linkTagRepository.addTagToLink(link.getId(), tag.id());
            tagNames.add(tag.name());
            logger.atInfo()
                    .addKeyValue("tagId", tag.id())
                    .addKeyValue("tagName", tag.name())
                    .log("Tag added");
        }

        logger.atInfo()
                .addKeyValue("chatId", chatId)
                .addKeyValue("url", request.url())
                .addKeyValue("tags", tagNames)
                .log("Link added");

        return new LinkResponse(link.getId(), link.getUrl(), tagNames, List.of());
    }

    @Transactional
    public LinkResponse removeLinkFromChat(Long chatId, RemoveLinkRequest request) {
        TrackedLink link = linkRepository.find(chatId, request.url()).orElseThrow(() -> {
            logger.atWarn()
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("url", request.url())
                    .log("Link not found when deleting");
            return new LinkNotFoundException(request.url());
        });

        List<String> tagNames = getTagNamesForLink(link.getId());
        linkRepository.remove(chatId, request.url());

        logger.atInfo()
                .addKeyValue("chatId", chatId)
                .addKeyValue("url", request.url())
                .log("Ссылка удалена");

        return new LinkResponse(link.getId(), link.getUrl(), tagNames, List.of());
    }

    private List<String> getTagNamesForLink(Integer linkId) {
        return linkTagRepository.findTagIdsByLinkId(linkId).stream()
                .map(tagId -> tagRepository.findById(tagId))
                .flatMap(Optional::stream)
                .map(tag -> tag.name())
                .toList();
    }
}
