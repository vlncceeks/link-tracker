package backend.academy.linktracker.scrapper;

import backend.academy.linktracker.scrapper.infrastructure.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
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

    @BeforeEach
    void setUp() {
        chatService.register(1L);
    }

    @Test
    void shouldReturn429_whenRateLimitExceeded() throws Exception {
        int limit = 3;

        for (int i = 0; i < limit; i++) {
            mockMvc.perform(get("/links").header("Tg-Chat-Id", 1L))
                .andExpect(status().isOk());
        }

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1L))
            .andExpect(status().isTooManyRequests());
    }

    @Test
    void shouldProcessRequests_withinRateLimit() throws Exception {
        int limit = 3;

        for (int i = 0; i < limit; i++) {
            mockMvc.perform(get("/links").header("Tg-Chat-Id", 1L))
                .andExpect(status().isOk());
        }
    }
}
