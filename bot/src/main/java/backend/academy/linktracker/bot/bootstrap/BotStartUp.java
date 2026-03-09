package backend.academy.linktracker.bot.bootstrap;

import backend.academy.linktracker.bot.application.client.ScrapperClient;
import backend.academy.linktracker.bot.application.command.CommandRepository;
import backend.academy.linktracker.bot.application.state.TrackDialogHandler;
import backend.academy.linktracker.bot.application.state.TrackSessionRepository;
import com.pengrad.telegrambot.TelegramBot;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class BotStartUp {
    private final TelegramBot bot;
    private final CommandRepository commandRepository;
    private final TrackSessionRepository trackSessionRepository;
    private final TrackDialogHandler trackDialogHandler;
    private final ScrapperClient scrapperClient;

    public BotStartUp(
            CommandRepository commandRepository,
            TelegramBot bot,
            TrackSessionRepository trackSessionRepository,
            TrackDialogHandler trackDialogHandler,
            ScrapperClient scrapperClient) {
        this.commandRepository = commandRepository;
        this.bot = bot;
        this.trackSessionRepository = trackSessionRepository;
        this.trackDialogHandler = trackDialogHandler;
        this.scrapperClient = scrapperClient;
    }

    @PostConstruct
    public void start() {
        BotRunner.run(commandRepository, bot, trackSessionRepository, trackDialogHandler, scrapperClient);
    }
}
