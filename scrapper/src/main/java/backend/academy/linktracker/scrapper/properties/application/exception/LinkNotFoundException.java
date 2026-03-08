package backend.academy.linktracker.scrapper.properties.application.exception;

public class LinkNotFoundException extends RuntimeException {
    public LinkNotFoundException(String url) {
        super("Ссылка не найдена: " + url);
    }
}
