package backend.academy.linktracker.bot.application.state;

import backend.academy.linktracker.bot.application.client.ScrapperClient;
import backend.academy.linktracker.bot.application.dto.request.AddLinkRequest;
import backend.academy.linktracker.bot.application.dto.request.RemoveLinkRequest;
import backend.academy.linktracker.bot.application.exception.ScrapperClientException;
import backend.academy.linktracker.bot.application.exception.SessionNotFoundException;
import backend.academy.linktracker.bot.infrastructure.service.SessionService;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrackDialogHandler {
    private static final Logger logger = LoggerFactory.getLogger(TrackDialogHandler.class);

    private final ScrapperClient scrapperClient;
    private final SessionService sessionService;

    public String handle(Long chatId, String text) {
        TrackSession session;
        try {
            session = sessionService.getTrackSession(chatId);
        } catch (SessionNotFoundException e) {
            return e.getMessage();
        }
        if (text.equals("/cancel")) {
            deleteSession(chatId);
            logger.atInfo().addKeyValue("chatId", chatId).log("Диалог /track отменён");
            return "Операция отменена.";
        }

        return switch (session.getState()) {
            case WAITING_FOR_URL -> handleUrl(chatId, text, session);
            case WAITING_FOR_TAGS -> handleTags(chatId, text, session);
        };
    }

    private String handleUrl(Long chatId, String text, TrackSession session) {
        if (!isValidUrl(text)) {
            return "Некорректный URL. Попробуйте ещё раз или введите /cancel для отмены:";
        }

        session.setUrl(text);
        logger.atDebug().addKeyValue("chatId", chatId).addKeyValue("url", text).log("URL получен");
        if (session.getCommandType() == TrackCommandType.UNTRACK) {
            sessionService.delete(chatId);
            try {
                scrapperClient.removeLink(chatId, new RemoveLinkRequest(text));
                return "Ссылка успешно удалена";
            } catch (ScrapperClientException e) {
                return "Ошибка: " + e.getMessage();
            }
        }
        session.setState(TrackState.WAITING_FOR_TAGS);
        sessionService.createTrackSession(chatId, session);

        return "Введите теги или нажмите /skip:";
    }

    private String handleTags(Long chatId, String text, TrackSession session) {
        List<String> tags = text.equals("/skip")
                ? List.of()
                : Arrays.stream(text.split(","))
                        .map(String::trim)
                        .filter(t -> !t.isBlank())
                        .toList();

        try {
            scrapperClient.addLink(chatId, new AddLinkRequest(session.getUrl(), tags, List.of()));

            logger.atInfo()
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("url", session.getUrl())
                    .addKeyValue("tags", tags)
                    .log("Ссылка добавлена через диалог /track");

            return "Ссылка добавлена для отслеживания: " + session.getUrl()
                    + (tags.isEmpty() ? "" : "\nТеги: " + String.join(", ", tags));

        } catch (ScrapperClientException e) {
            logger.atWarn()
                    .addKeyValue("chatId", chatId)
                    .addKeyValue("url", session.getUrl())
                    .log("Ошибка при добавлении ссылки");
            return "Ошибка при добавлении ссылки: " + e.getMessage();
        } finally {
            sessionService.delete(chatId);
        }
    }

    private boolean isValidUrl(String text) {
        return text.startsWith("https://github.com/") || text.startsWith("https://stackoverflow.com/questions/");
    }

    public boolean hasSession(Long chatId) {
        return sessionService.hasTrackSession(chatId);
    }

    public void deleteSession(Long chatId) {
        sessionService.delete(chatId);
    }
}
