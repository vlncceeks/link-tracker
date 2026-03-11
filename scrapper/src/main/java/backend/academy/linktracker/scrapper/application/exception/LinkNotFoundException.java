package backend.academy.linktracker.scrapper.application.exception;

public class LinkNotFoundException extends RuntimeException {
    public LinkNotFoundException(String url) {
        super("Ссылка не найдена: " + url);
    }
}
