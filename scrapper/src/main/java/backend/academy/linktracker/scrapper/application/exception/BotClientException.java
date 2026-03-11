package backend.academy.linktracker.scrapper.application.exception;

public class BotClientException extends RuntimeException {
    public BotClientException(String message) {
        super(message);
    }
}
