package backend.academy.linktracker.bot.infrastructure.registry;

import backend.academy.linktracker.bot.application.command.Command;
import backend.academy.linktracker.bot.application.command.CommandRepository;
import org.springframework.stereotype.Repository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class InMemoryCommandRepository implements CommandRepository {

    private final HashMap<String, Command> repository = new HashMap<>();

    @Override
    public void addCommand(Command command) {
        repository.putIfAbsent(command.getName(), command);
    }

    @Override
    public Optional<Command> findCommand(String commandName) {
        return Optional.ofNullable(repository.get(commandName));
    }

    @Override
    public Set<Command> getAllCommands() {
        return new HashSet<>(repository.values());
    }
}
