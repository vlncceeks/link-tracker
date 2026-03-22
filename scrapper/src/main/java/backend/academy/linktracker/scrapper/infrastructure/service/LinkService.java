package backend.academy.linktracker.scrapper.infrastructure.service;

import backend.academy.linktracker.scrapper.application.dto.request.AddLinkRequest;
import backend.academy.linktracker.scrapper.application.dto.request.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.application.dto.response.LinkResponse;
import backend.academy.linktracker.scrapper.application.dto.response.ListLinksResponse;
import backend.academy.linktracker.scrapper.application.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.application.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.application.link.LinkRepository;
import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LinkService {
    private static final Logger logger = LoggerFactory.getLogger(LinkService.class);
    private final LinkRepository linkRepository;

    public ListLinksResponse getAllByChatId(Long chatId) {
        List<LinkResponse> links = linkRepository.findAll(chatId).stream()
                .map(l -> new LinkResponse(
                        l.getId(), l.getUrl(), new ArrayList<>(l.getTags()), new ArrayList<>(l.getFilters())))
                .toList();
        return new ListLinksResponse(links, links.size());
    }

    public LinkResponse addLinkIntoChat(Long chatId, AddLinkRequest request) {
        if (linkRepository.find(chatId, request.url()).isPresent()) {
            throw new LinkAlreadyTrackedException(request.url());
        }

        TrackedLink link = linkRepository.add(
                chatId,
                new TrackedLink(
                        null, chatId, request.url(), new HashSet<>(request.tags()), new HashSet<>(request.filters())));

        logger.atInfo()
                .addKeyValue("chatId", chatId)
                .addKeyValue("url", request.url())
                .log("Ссылка добавлена");

        return new LinkResponse(
                link.getId(), link.getUrl(), new ArrayList<>(link.getTags()), new ArrayList<>(link.getFilters()));
    }

    public LinkResponse removeLinkFromChat(Long chatId, RemoveLinkRequest request) {
        TrackedLink link =
                linkRepository.find(chatId, request.url()).orElseThrow(() -> new LinkNotFoundException(request.url()));
        linkRepository.remove(chatId, request.url());

        logger.atInfo()
                .addKeyValue("chatId", chatId)
                .addKeyValue("url", request.url())
                .log("Ссылка удалена");

        return new LinkResponse(
                link.getId(), link.getUrl(), new ArrayList<>(link.getTags()), new ArrayList<>(link.getFilters()));
    }
}
