package backend.academy.linktracker.bot.application.state;

import java.util.Optional;

public interface TrackSessionRepository {
    void save(Long chatId, TrackSession session);
    Optional<TrackSession> find(Long chatId);
    void delete(Long chatId);
    boolean hasSession(Long chatId);
}
