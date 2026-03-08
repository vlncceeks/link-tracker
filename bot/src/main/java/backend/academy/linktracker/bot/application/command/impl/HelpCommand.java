package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.client.ScrapperClient;
import backend.academy.linktracker.bot.application.command.Command;
import backend.academy.linktracker.bot.application.command.CommandRepository;

public class HelpCommand implements Command {
    private final CommandRepository repository;
    private final ScrapperClient scrapperClient;

    public HelpCommand(CommandRepository repository, ScrapperClient scrapperClient) {
        this.repository = repository;
        this.scrapperClient = scrapperClient;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String execute(String username, Long chatId, String[] args) {
        String message = "Доступные команды:\n"
                + repository.getAllCommands().stream()
                        .map(command -> "/" + command.getName())
                        .reduce((a, b) -> a + "\n" + b)
                        .orElse("Нет доступных команд");
        return message;
    }
}
