package backend.academy.linktracker.bot.application.exception;

public class CommandNotFoundException extends RuntimeException {
    public CommandNotFoundException(String message) {
        super(message);
    }
}
