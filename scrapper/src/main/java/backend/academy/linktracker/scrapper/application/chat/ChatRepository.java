package backend.academy.linktracker.scrapper.application.chat;

public interface ChatRepository {
    void register(Long chatId);

    void delete(Long chatId);

    boolean exists(Long chatId);
}
