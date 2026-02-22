package backend.academy.linktracker.bot.repository;

import backend.academy.linktracker.bot.command.Command;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class InMemoryCommandRepository implements CommandRepository {

    private final HashMap<String, Command> repository = new HashMap<>();

    @Override
    public void addCommand(Command command) {
        repository.put(command.getName(), command);
    }

    @Override
    public Optional<Command> getCommand(String commandName) {
        return Optional.ofNullable(repository.get(commandName));
    }

    @Override
    public Set<Command> getAllCommands() {
        return repository.values().stream().collect(Collectors.toSet());
    }
}
