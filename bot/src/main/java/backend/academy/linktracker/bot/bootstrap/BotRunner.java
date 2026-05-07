package backend.academy.linktracker.bot.bootstrap;

import backend.academy.linktracker.bot.application.command.CommandDispatcher;
import backend.academy.linktracker.bot.application.state.TrackDialogHandler;
import backend.academy.linktracker.bot.application.state.TrackSessionRepository;
import backend.academy.linktracker.bot.infrastructure.service.CommandService;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotRunner {
    private static final Logger logger = LoggerFactory.getLogger(BotRunner.class);

    public static void run(
            CommandService commandService,
            TelegramBot bot,
            TrackSessionRepository trackSessionRepository,
            TrackDialogHandler trackDialogHandler) {
        CommandDispatcher dispatcher = new CommandDispatcher(commandService, bot, trackDialogHandler);
        logger.atInfo().log("Диспетчер инициализирован");

        bot.setUpdatesListener(updates -> {
            updates.forEach(dispatcher::handleUpdate);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
        logger.atInfo().log("UpdatesListener установлен");
    }
}
