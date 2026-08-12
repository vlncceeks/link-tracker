package backend.academy.linktracker.scrapper.infrastructure.service;

import backend.academy.linktracker.scrapper.application.dto.request.AddLinkRequest;
import backend.academy.linktracker.scrapper.application.dto.request.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.application.dto.response.LinkResponse;
import backend.academy.linktracker.scrapper.application.dto.response.ListLinksResponse;
import backend.academy.linktracker.scrapper.application.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.application.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.application.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.application.exception.TagAlreadyAddedException;
import backend.academy.linktracker.scrapper.application.link.LinkRepository;
import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import backend.academy.linktracker.scrapper.application.linktag.LinkTagRepository;
import backend.academy.linktracker.scrapper.application.tag.Tag;
import backend.academy.linktracker.scrapper.application.tag.TagRepository;
import backend.academy.linktracker.scrapper.infrastructure.configuration.SchedulerProperties;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LinkService {
    private static final Logger logger = LoggerFactory.getLogger(LinkService.class);
    private final LinkRepository linkRepository;
    private final TagRepository tagRepository;
    private final LinkTagRepository linkTagRepository;
    private final SchedulerProperties properties;
    private final ChatService chatService;

    @Cacheable(value = "Tg-Chat-Id", key = "#chatId")
    public ListLinksResponse getAllByChatId(Long chatId) {
        List<LinkResponse> allLinks = new ArrayList<>();
        long lastId = 0;

        while (true) {
            List<LinkResponse> batch = linkRepository.findAllWithTags(chatId, properties.batchSize(), lastId);

            if (batch.isEmpty()) break;

            allLinks.addAll(batch);
            lastId = batch.getLast().id();

            if (batch.size() < properties.batchSize()) break;
        }

        logger.atDebug()
                .addKeyValue("chatId", chatId)
                .addKeyValue("count", allLinks.size())
                .log("Receive list of links");

        return new ListLinksResponse(allLinks, allLinks.size());
    }

    @Transactional
    @CacheEvict(value = "Tg-Chat-Id", key = "#chatId")
    public LinkResponse addLinkIntoChat(Long chatId, AddLinkRequest request) {
        if (!chatService.exists(chatId)) {
            throw new ChatNotFoundException(chatId);
        }

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
            Tag tag = tagRepository.findByName(tagName).orElseGet(() -> {
                try {
                    return tagRepository.add(tagName);
                } catch (DuplicateKeyException e) {
                    throw new TagAlreadyAddedException("Тег: " + tagName + " уже существует.");
                }
            });

            try {
                linkTagRepository.addTagToLink(link.getId(), tag.getId());
            } catch (DuplicateKeyException e) {
                throw new TagAlreadyAddedException("Ссылка с тегом: " + tag.getName() + " уже существует.");
            }

            tagNames.add(tag.getName());
            logger.atInfo()
                    .addKeyValue("tagId", tag.getId())
                    .addKeyValue("tagName", tag.getName())
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
    @CacheEvict(value = "Tg-Chat-Id", key = "#chatId")
    public LinkResponse removeLinkFromChat(Long chatId, RemoveLinkRequest request) {
        if (!chatService.exists(chatId)) {
            throw new ChatNotFoundException(chatId);
        }

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
        List<Integer> allTagIds = new ArrayList<>();
        long lastId = 0;

        while (true) {
            List<Integer> batch = linkTagRepository.findTagIdsByLinkId(linkId, properties.batchSize(), lastId);

            if (batch.isEmpty()) break;

            allTagIds.addAll(batch);
            lastId = batch.getLast();

            if (batch.size() < properties.batchSize()) break;
        }

        return allTagIds.stream()
                .map(tagId -> tagRepository.findById(tagId))
                .flatMap(Optional::stream)
                .map(tag -> tag.getName())
                .toList();
    }
}
