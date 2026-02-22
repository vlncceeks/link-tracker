package backend.academy.linktracker.bot.repository;

import backend.academy.linktracker.bot.command.Command;
import java.util.Optional;
import java.util.Set;

public interface CommandRepository {
    void addCommand(Command command);

    Optional<Command> getCommand(String commandName);

    Set<Command> getAllCommands();
}
