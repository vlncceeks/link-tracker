package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.command.Command;

public class StartCommand implements Command {
    @Override
    public String getName() {
        return "start";
    }

    @Override
    public String execute(String username, String[] args) {
        String message = "Добро пожаловать, " + username + "! Используйте /help, чтобы посмотреть доступные команды.";
        return message;
    }
}
