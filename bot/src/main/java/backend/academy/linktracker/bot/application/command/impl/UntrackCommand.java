package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.client.ScrapperClient;
import backend.academy.linktracker.bot.application.command.Command;
import backend.academy.linktracker.bot.application.state.TrackCommandType;
import backend.academy.linktracker.bot.application.state.TrackSession;
import backend.academy.linktracker.bot.application.state.TrackSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UntrackCommand implements Command {
    private final ScrapperClient scrapperClient;
    private final TrackSessionRepository sessionRepository;

    @Override
    public String getName() {
        return "untrack";
    }

    @Override
    public String getDescription() {
        return "Прекратить отслеживание ссылки";
    }

    @Override
    public String execute(String username, Long chatId, String[] args) {
        sessionRepository.save(chatId, new TrackSession(TrackCommandType.UNTRACK));
        return "Введите URL ссылки для удаления:";
    }
}
