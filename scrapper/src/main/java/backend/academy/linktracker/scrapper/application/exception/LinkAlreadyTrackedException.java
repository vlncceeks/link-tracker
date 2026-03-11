package backend.academy.linktracker.scrapper.application.exception;

public class LinkAlreadyTrackedException extends RuntimeException {
    public LinkAlreadyTrackedException(String url) {
        super("Ссылка уже отслеживается: " + url);
    }
}
