package backend.academy.linktracker.bot.runner;

import backend.academy.linktracker.bot.command.HelpCommand;
import backend.academy.linktracker.bot.command.StartCommand;
import backend.academy.linktracker.bot.dispatcher.CommandDispatcher;
import backend.academy.linktracker.bot.repository.CommandRepository;
import backend.academy.linktracker.bot.repository.InMemoryCommandRepository;
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
