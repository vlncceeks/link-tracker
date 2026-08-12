package backend.academy.linktracker.bot.infrastructure.service;

import backend.academy.linktracker.bot.application.exception.SessionNotFoundException;
import backend.academy.linktracker.bot.application.state.TrackSession;
import backend.academy.linktracker.bot.application.state.TrackSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final TrackSessionRepository sessionRepository;

    public void createTrackSession(Long chatId, TrackSession session) {
        sessionRepository.save(chatId, session);
    }

    public TrackSession getTrackSession(Long chatId) {
        return sessionRepository.find(chatId).orElseThrow(() -> new SessionNotFoundException("Сессия не найдена"));
    }

    public void delete(Long chatId) {
        sessionRepository.delete(chatId);
    }

    public boolean hasTrackSession(Long chatId) {
        return sessionRepository.hasSession(chatId);
    }
}
