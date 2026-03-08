package backend.academy.linktracker.scrapper.properties.application.exception;

public class ChatNotFoundException extends RuntimeException {
    public ChatNotFoundException(Long id) {
        super("Чат не найден: " + id);
    }
}
