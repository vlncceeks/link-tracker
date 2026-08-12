package backend.academy.linktracker.scrapper.application.exception;

public class TagAlreadyAddedException extends RuntimeException {
    public TagAlreadyAddedException(String message) {
        super(message);
    }
}
