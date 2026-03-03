package backend.academy.linktracker.bot.application.command;

public interface Command {
    String getName();

    String execute(String username, String[] args);
}
