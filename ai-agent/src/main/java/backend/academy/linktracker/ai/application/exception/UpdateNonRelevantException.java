package backend.academy.linktracker.ai.application.exception;

public class UpdateNonRelevantException extends RuntimeException {
    public UpdateNonRelevantException(String message) {
        super(message);
    }
}
