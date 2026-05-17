package backend.academy.linktracker.scrapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.infrastructure.service.ChatService;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
@TestPropertySource(
        properties = {
            "app.access-type=MEMORY",
            "app.message-sender-type=DIRECTLY",
            "app.rate-limit.requests-per-second=3",
            "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration"
        })
@ContextConfiguration(initializers = TestRedisConfiguration.class)
class RateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChatService chatService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        chatService.register(1L);
        Set<String> keys = redisTemplate.keys("rate-limit:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    void shouldReturn429_whenRateLimitExceeded() throws Exception {
        int limit = 3;

        for (int i = 0; i < limit; i++) {
            mockMvc.perform(get("/links").header("Tg-Chat-Id", 1L)).andExpect(status().isOk());
        }

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1L)).andExpect(status().isTooManyRequests());
    }

    @Test
    void shouldProcessRequests_withinRateLimit() throws Exception {
        int limit = 3;

        for (int i = 0; i < limit; i++) {
            mockMvc.perform(get("/links").header("Tg-Chat-Id", 1L)).andExpect(status().isOk());
        }
    }
}
