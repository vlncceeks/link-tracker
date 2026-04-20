package backend.academy.linktracker.bot.infrastructure.service;

import backend.academy.linktracker.bot.application.dto.request.LinkUpdateRequest;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateService {
    private final TelegramBot bot;

    public void receive(LinkUpdateRequest request) {
        String message = "Обновление по ссылке: " + request.url()
                + (request.description() != null ? "\n" + request.description() : "");

        for (Long chatId : request.chatIds()) {
            bot.execute(new SendMessage(chatId, message));
        }
    }
}
