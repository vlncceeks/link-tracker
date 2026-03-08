package backend.academy.linktracker.bot.infrastructure.api;

import backend.academy.linktracker.bot.application.dto.request.LinkUpdateRequest;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UpdateController {
    private static final Logger logger = LoggerFactory.getLogger(UpdateController.class);
    private final TelegramBot bot;

    public UpdateController(TelegramBot bot) {
        this.bot = bot;
    }

    @PostMapping("/updates")
    public ResponseEntity<Void> receiveUpdate(@RequestBody @Valid LinkUpdateRequest request) {
        logger.atInfo()
            .addKeyValue("url", request.url())
            .addKeyValue("chatCount", request.tgChatIds().size())
            .log("Получено обновление ссылки");

        String message = "Обновление по ссылке: " + request.url()
            + (request.description() != null ? "\n" + request.description() : "");

        for (Long chatId : request.tgChatIds()) {
            bot.execute(new SendMessage(chatId, message));
        }
        return ResponseEntity.ok().build();
    }
}
