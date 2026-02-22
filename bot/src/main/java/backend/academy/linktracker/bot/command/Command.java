package backend.academy.linktracker.bot.command;

import com.pengrad.telegrambot.TelegramBot;

public interface Command {
    String getName();

    void execute(Long chatId, String username, String[] args, TelegramBot bot);
}
