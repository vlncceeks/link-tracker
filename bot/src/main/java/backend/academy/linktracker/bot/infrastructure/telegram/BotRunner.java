package backend.academy.linktracker.bot.infrastructure.telegram;

import backend.academy.linktracker.bot.application.command.CommandDispatcher;
import backend.academy.linktracker.bot.application.command.CommandRepository;
import backend.academy.linktracker.bot.application.command.impl.HelpCommand;
import backend.academy.linktracker.bot.application.command.impl.StartCommand;
import backend.academy.linktracker.bot.bootstrap.BotCommandsSetup;
import backend.academy.linktracker.bot.infrastructure.registry.InMemoryCommandRepository;
import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotRunner {
    private static final Logger logger = LoggerFactory.getLogger(BotRunner.class);

    public static void run(TelegramBot bot) {
        CommandRepository repository = new InMemoryCommandRepository();
        repository.addCommand(new StartCommand());
        repository.addCommand(new HelpCommand(repository));

        CommandDispatcher dispatcher = new CommandDispatcher(repository, bot);
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
