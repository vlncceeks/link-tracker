package backend.academy.linktracker.bot.bootstrap;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;

public class BotCommandsSetup {
    public static void setupCommands(TelegramBot bot) {
        BotCommand[] commands = {
            new BotCommand("start", "Начать работу с ботом"),
            new BotCommand("help", "Показать список команд"),
            new BotCommand("track", "Начать отслеживание ссылки"),
            new BotCommand("untrack", "Прекратить отслеживание ссылки"),
            new BotCommand("list", "Вывести список всех ссылок, отслеживаемых пользователем")
        };

        bot.execute(new SetMyCommands(commands));
    }
}
