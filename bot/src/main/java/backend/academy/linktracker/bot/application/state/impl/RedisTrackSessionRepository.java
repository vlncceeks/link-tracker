package backend.academy.linktracker.bot.application.state.impl;

import backend.academy.linktracker.bot.application.state.TrackSession;
import backend.academy.linktracker.bot.application.state.TrackSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;

@Primary
@Repository
@RequiredArgsConstructor
public class RedisTrackSessionRepository implements TrackSessionRepository {
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    @Override
    public void save(Long chatId, TrackSession session) {
        String data = objectMapper.writeValueAsString(session);
        redis.opsForValue().set("chat:" + chatId, data, Duration.ofMinutes(30));
    }

    @Override
    public Optional<TrackSession> find(Long chatId) {
        String data = redis.opsForValue().get("chat:" + chatId);
        if (data == null) throw new IllegalStateException("Сессия не найдена");
        TrackSession session = objectMapper.readValue(data, TrackSession.class);
        return Optional.ofNullable(session);
    }

    @Override
    public void delete(Long chatId) {
        redis.delete("chat:" + chatId);
    }

    @Override
    public boolean hasSession(Long chatId) {
        String data = redis.opsForValue().get("chat:" + chatId);
        if (data == null) return false;
        return true;
    }
}
