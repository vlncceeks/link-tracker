package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.command.Command;
import backend.academy.linktracker.bot.application.state.TrackSession;
import backend.academy.linktracker.bot.application.state.TrackSessionRepository;

public class TrackCommand implements Command {
    private final TrackSessionRepository sessionRepository;

    public TrackCommand(TrackSessionRepository sessionRepository) {
        this.sessionRepository = sessionRepository;
    }
    @Override
    public String getName() {
        return "track";
    }

    @Override
    public String execute(String username, Long chatId, String[] args) {
        sessionRepository.save(chatId, new TrackSession());
        return "Введите URL ссылки для отслеживания:";
    }
}
