package backend.academy.linktracker.scrapper.properties.infrastructure.api;

import backend.academy.linktracker.scrapper.properties.application.chat.ChatRepository;
import backend.academy.linktracker.scrapper.properties.application.dto.request.AddLinkRequest;
import backend.academy.linktracker.scrapper.properties.application.dto.request.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.properties.application.dto.response.LinkResponse;
import backend.academy.linktracker.scrapper.properties.application.dto.response.ListLinksResponse;
import backend.academy.linktracker.scrapper.properties.application.exception.LinkAlreadyTrackedException;
import backend.academy.linktracker.scrapper.properties.application.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.properties.application.link.TrackedLink;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ScrapperController {
    private static final Logger logger = LoggerFactory.getLogger(ScrapperController.class);
    private final ChatRepository chatRepository;

    @PostMapping("/tg-chat/{id}")
    public ResponseEntity<Void> registerChat(@PathVariable Long id) {
        logger.atInfo().addKeyValue("chatId", id).log("Chat registration");
        chatRepository.register(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tg-chat/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable Long id) {
        logger.atInfo().addKeyValue("chatId", id).log("Удаление чата");
        chatRepository.delete(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/links")
    public ResponseEntity<ListLinksResponse> getLinks(@RequestHeader("Tg-Chat-Id") Long chatId) {
        List<LinkResponse> links = chatRepository.getLinks(chatId).stream()
            .map(l -> new LinkResponse(
                    l.getId(),
                    l.getUrl(),
                    new ArrayList<>(l.getTags()),
                    new ArrayList<>(l.getFilters())
                )
            ).toList();
        return ResponseEntity.ok(new ListLinksResponse(links, links.size()));
    }

    @PostMapping("/links")
    public ResponseEntity<LinkResponse> addLink(
        @RequestHeader("Tg-Chat-Id") Long chatId,
        @RequestBody AddLinkRequest request) {

        if (chatRepository.findLink(chatId, request.url()).isPresent()) {
            throw new LinkAlreadyTrackedException(request.url());
        }

        TrackedLink link = new TrackedLink(
            chatRepository.nextId(), request.url(),
            request.tags(), request.filters()
        );
        chatRepository.addLink(chatId, link);

        logger.atInfo()
            .addKeyValue("chatId", chatId)
            .addKeyValue("url", request.url())
            .log("Ссылка добавлена");

        return ResponseEntity.ok(
            new LinkResponse(link.getId(), link.getUrl(),
                new ArrayList<>(link.getTags()), new ArrayList<>(link.getFilters()))
        );
    }

    @DeleteMapping("/links")
    public ResponseEntity<LinkResponse> removeLink(
        @RequestHeader("Tg-Chat-Id") Long chatId,
        @RequestBody RemoveLinkRequest request) {

        TrackedLink link = chatRepository.findLink(chatId, request.url())
            .orElseThrow(() -> new LinkNotFoundException(request.url()));
        chatRepository.removeLink(chatId, request.url());

        logger.atInfo()
            .addKeyValue("chatId", chatId)
            .addKeyValue("url", request.url())
            .log("Ссылка удалена");

        return ResponseEntity.ok(
            new LinkResponse(link.getId(), link.getUrl(),
                new ArrayList<>(link.getTags()), new ArrayList<>(link.getFilters()))
        );
    }
}
