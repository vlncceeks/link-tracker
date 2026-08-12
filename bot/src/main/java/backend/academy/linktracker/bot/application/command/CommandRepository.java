package backend.academy.linktracker.bot.application.command;

import java.util.Optional;
import java.util.Set;

public interface CommandRepository {
    void addCommand(Command command);

    Optional<Command> getCommand(String commandName);

    Set<Command> getAllCommands();
}
