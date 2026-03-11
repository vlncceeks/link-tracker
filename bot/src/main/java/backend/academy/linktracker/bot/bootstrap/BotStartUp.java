package backend.academy.linktracker.bot.bootstrap;

import backend.academy.linktracker.bot.application.command.CommandRepository;
import backend.academy.linktracker.bot.application.state.TrackDialogHandler;
import backend.academy.linktracker.bot.application.state.TrackSessionRepository;
import com.pengrad.telegrambot.TelegramBot;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class BotStartUp {
    private final TelegramBot bot;
    private final CommandRepository commandRepository;
    private final TrackSessionRepository trackSessionRepository;
    private final TrackDialogHandler trackDialogHandler;

    @PostConstruct
    public void start() {
        BotRunner.run(commandRepository, bot, trackSessionRepository, trackDialogHandler);
    }
}
