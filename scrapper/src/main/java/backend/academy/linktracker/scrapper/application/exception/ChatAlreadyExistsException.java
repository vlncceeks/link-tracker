package backend.academy.linktracker.scrapper.application.exception;

public class ChatAlreadyExistsException extends RuntimeException {
    public ChatAlreadyExistsException(Long id) {
        super("Чат уже существует: " + id);
    }
}
