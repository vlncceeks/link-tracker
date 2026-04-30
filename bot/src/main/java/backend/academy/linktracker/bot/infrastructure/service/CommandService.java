package backend.academy.linktracker.bot.infrastructure.service;

import backend.academy.linktracker.bot.application.command.Command;
import backend.academy.linktracker.bot.application.command.CommandRepository;
import backend.academy.linktracker.bot.application.exception.CommandNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommandService {
    private final CommandRepository repository;

    public void createCommand(Command command) {
        repository.addCommand(command);
    }

    public Command getCommand(String commandName) {
        return repository.getCommand(commandName).orElseThrow(() -> new CommandNotFoundException(commandName));
    }
}
