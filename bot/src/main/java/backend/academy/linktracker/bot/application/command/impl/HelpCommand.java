package backend.academy.linktracker.bot.application.command.impl;

import backend.academy.linktracker.bot.application.command.Command;
import backend.academy.linktracker.bot.application.command.CommandRepository;
import org.springframework.stereotype.Component;

@Component
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
    public String getDescription() {
        return "Показать список команд";
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
