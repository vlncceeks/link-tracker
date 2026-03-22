package backend.academy.linktracker.scrapper.application.chat;

import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ChatRepository {
    void register(Long chatId);

    void delete(Long chatId);

    boolean exists(Long chatId);

}
