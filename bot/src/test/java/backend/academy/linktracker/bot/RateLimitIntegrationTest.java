package backend.academy.linktracker.bot;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@SpringBootTest
@EnableWireMock(@ConfigureWireMock(port = 8089))
@DirtiesContext
@TestPropertySource(properties = {"bot.base-url=http://localhost:8089", "app.rate-limit.requests-per-second=3"})
@ContextConfiguration(initializers = TestcontainersConfiguration.class)
public class RateLimitIntegrationTest {

    @InjectWireMock
    private WireMockServer wireMock;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        stubFor(WireMock.post(urlMatching("/bot[^/]+/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"ok\":true,\"result\":true}")));
        Set<String> keys = redisTemplate.keys("rate-limit:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    void shouldProcessRequests_withinRateLimit() throws Exception {
        int limit = 3;

        for (int i = 0; i < limit; i++) {
            stubFor(
                    WireMock.post(urlEqualTo("/updates"))
                            .willReturn(
                                    aResponse()
                                            .withStatus(200)
                                            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                            .withBody(
                                                    "{\"id\":1,\"url\":\"https://github.com\",\"description\":\"test\",\"chatIds\":[1]}")));
        }
    }

    @Test
    void shouldReturn429_whenRateLimitExceeded() throws Exception {
        int limit = 3;

        for (int i = 0; i < limit; i++) {
            stubFor(
                    WireMock.post(urlEqualTo("/updates"))
                            .willReturn(
                                    aResponse()
                                            .withStatus(200)
                                            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                            .withBody(
                                                    "{\"id\":1,\"url\":\"https://github.com\",\"description\":\"test\",\"chatIds\":[1]}")));
        }

        stubFor(WireMock.post(urlEqualTo("/updates"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.TOO_MANY_REQUESTS.value())
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(
                                "{\"id\":1,\"url\":\"https://github.com\",\"description\":\"test\",\"chatIds\":[1]}")));
    }
}
