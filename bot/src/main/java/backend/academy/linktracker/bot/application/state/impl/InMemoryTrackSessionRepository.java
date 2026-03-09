package backend.academy.linktracker.bot.application.state.impl;

import backend.academy.linktracker.bot.application.state.TrackSession;
import backend.academy.linktracker.bot.application.state.TrackSessionRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryTrackSessionRepository implements TrackSessionRepository {
    private final Map<Long, TrackSession> sessions = new HashMap<>();

    @Override
    public void save(Long chatId, TrackSession session) {
        sessions.put(chatId, session);
    }

    @Override
    public Optional<TrackSession> find(Long chatId) {
        return Optional.ofNullable(sessions.get(chatId));
    }

    @Override
    public void delete(Long chatId) {
        sessions.remove(chatId);
    }

    @Override
    public boolean hasSession(Long chatId) {
        return sessions.containsKey(chatId);
    }
}
