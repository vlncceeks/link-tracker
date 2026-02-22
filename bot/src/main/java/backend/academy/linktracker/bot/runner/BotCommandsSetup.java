package backend.academy.linktracker.bot.runner;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.request.SetMyCommands;

public class BotCommandsSetup {
    public static void setupCommands(TelegramBot bot) {
        BotCommand[] commands = {
            new BotCommand("start", "Начать работу с ботом"), new BotCommand("help", "Показать список команд")
        };

        bot.execute(new SetMyCommands(commands));
    }
}
