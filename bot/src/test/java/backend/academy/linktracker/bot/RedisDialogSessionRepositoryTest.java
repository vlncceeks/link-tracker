package backend.academy.linktracker.bot;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.bot.application.state.TrackSession;
import backend.academy.linktracker.bot.application.state.TrackState;
import backend.academy.linktracker.bot.application.state.impl.RedisTrackSessionRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

@Testcontainers
class RedisDialogSessionRepositoryTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    private RedisTrackSessionRepository repository;

    @BeforeEach
    void setUp() {
        RedisStandaloneConfiguration config =
                new RedisStandaloneConfiguration(redis.getHost(), redis.getMappedPort(6379));
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(config);
        connectionFactory.afterPropertiesSet();

        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.afterPropertiesSet();

        repository = new RedisTrackSessionRepository(redisTemplate, new ObjectMapper());
    }

    @Test
    void shouldSaveAndFindSession() {
        TrackSession session = new TrackSession();

        repository.save(123L, session);
        Optional<TrackSession> result = repository.find(123L);

        assertThat(result).isPresent();
        assertThat(result.get().getState()).isEqualTo(TrackState.WAITING_FOR_URL);
        assertThat(result.get().getUrl()).isEqualTo("");
    }

    @Test
    void shouldReturnEmptyIfNotFound() {
        Optional<TrackSession> result = repository.find(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldDeleteSession() {
        TrackSession session = new TrackSession();
        repository.save(123L, session);

        repository.delete(123L);

        assertThat(repository.find(123L)).isEmpty();
    }

    @Test
    void shouldOverwriteExistingSession() {
        TrackSession session = new TrackSession();
        session.setState(TrackState.WAITING_FOR_TAGS);
        repository.save(123L, session);
        repository.save(123L, new TrackSession());

        Optional<TrackSession> result = repository.find(123L);

        assertThat(result).isPresent();
        assertThat(result.get().getState()).isEqualTo(TrackState.WAITING_FOR_URL);
    }
}
