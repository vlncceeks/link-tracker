package backend.academy.linktracker.bot.bootstrap;

import backend.academy.linktracker.bot.application.client.ScrapperClient;
import backend.academy.linktracker.bot.application.command.CommandDispatcher;
import backend.academy.linktracker.bot.application.command.CommandRepository;
import backend.academy.linktracker.bot.application.command.impl.HelpCommand;
import backend.academy.linktracker.bot.application.command.impl.ListCommand;
import backend.academy.linktracker.bot.application.command.impl.StartCommand;
import backend.academy.linktracker.bot.application.command.impl.TrackCommand;
import backend.academy.linktracker.bot.application.command.impl.UntrackCommand;
import backend.academy.linktracker.bot.application.state.TrackDialogHandler;
import backend.academy.linktracker.bot.application.state.TrackSessionRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotRunner {
    private static final Logger logger = LoggerFactory.getLogger(BotRunner.class);

    public static void run(
            CommandRepository commandRepository,
            TelegramBot bot,
            TrackSessionRepository trackSessionRepository,
            TrackDialogHandler trackDialogHandler,
            ScrapperClient scrapperClient) {
        commandRepository.addCommand(new StartCommand(scrapperClient));
        commandRepository.addCommand(new HelpCommand(commandRepository));
        commandRepository.addCommand(new ListCommand(scrapperClient));
        commandRepository.addCommand(new TrackCommand(trackSessionRepository));
        commandRepository.addCommand(new UntrackCommand(scrapperClient));

        CommandDispatcher dispatcher =
                new CommandDispatcher(commandRepository, bot, trackSessionRepository, trackDialogHandler);
        logger.atInfo().log("Команды и диспетчер инициализированы");

        BotCommandsSetup.setupCommands(bot);
        logger.atInfo().log("Настроено меню команд");

        bot.setUpdatesListener(updates -> {
            updates.forEach(dispatcher::handleUpdate);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
        logger.atInfo().log("UpdatesListener установлен");
    }
}
