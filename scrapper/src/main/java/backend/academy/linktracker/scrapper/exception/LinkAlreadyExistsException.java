package backend.academy.linktracker.scrapper.exception;

public class LinkAlreadyExistsException extends RuntimeException {
    public LinkAlreadyExistsException(String url) {
        super("Ссылка уже отслеживается: " + url);
    }
}
