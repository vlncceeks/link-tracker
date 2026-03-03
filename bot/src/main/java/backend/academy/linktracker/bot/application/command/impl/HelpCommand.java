package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.command.Command;
import backend.academy.linktracker.bot.application.command.CommandRepository;

public class HelpCommand implements Command {
    private final CommandRepository repository;

    public HelpCommand(CommandRepository repository) {
        this.repository = repository;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String execute(String username, String[] args) {
        String message = "Доступные команды:\n"
                + repository.getAllCommands().stream()
                        .map(command -> "/" + command.getName())
                        .reduce((a, b) -> a + "\n" + b)
                        .orElse("Нет доступных команд");
        return message;
    }
}
