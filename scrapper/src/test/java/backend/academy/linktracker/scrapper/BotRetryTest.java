package backend.academy.linktracker.scrapper;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.linktracker.scrapper.application.client.MessageSender;
import backend.academy.linktracker.scrapper.application.dto.request.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.application.exception.BotClientException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest
@ActiveProfiles("test")
@EnableWireMock(@ConfigureWireMock(port = 8089))
@TestPropertySource(
        properties = {
            "bot.base-url=http://localhost:8089",
            "spring.autoconfigure.exclude=" + "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,"
                    + "org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration,"
                    + "org.springframework.boot.data.redis.autoconfigure.RedisAutoConfiguration,"
                    + "org.springframework.boot.data.redis.autoconfigure.RedisRepositoriesAutoConfiguration,"
                    + "org.springframework.boot.data.redis.autoconfigure.health.DataRedisReactiveHealthContributorAutoConfiguration,"
                    + "org.springframework.boot.data.redis.autoconfigure.health.DataRedisHealthContributorAutoConfiguration,"
                    + "org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration",
            "app.access-type=MEMORY",
            "app.message-sender-type=DIRECTLY",
            "spring.cache.type=none"
        })
public class BotRetryTest {

    @MockitoBean(name = "kafkaMessageSender")
    private MessageSender kafkaSender;

    @Autowired
    private MessageSender messageSender;

    @Test
    public void send_shouldNotRetry_whenServiceReturns200() {
        stubFor(post(urlEqualTo("/updates")).willReturn(aResponse().withStatus(200)));

        messageSender.send(new LinkUpdateRequest(1, "https://github.com/user/repo", "коммит", List.of(1L)));

        verify(1, postRequestedFor(urlEqualTo("/updates")));
    }

    @Test
    public void send_shouldRetryThreeTimes_whenServiceReturns500() {
        stubFor(post(urlEqualTo("/updates"))
                .willReturn(aResponse().withStatus(500).withBody("{\"description\":\"error\"}")));

        messageSender.send(new LinkUpdateRequest(1, "https://github.com/user/repo", "коммит", List.of(1L)));

        verify(3, postRequestedFor(urlEqualTo("/updates")));
    }

    @Test
    public void send_shouldSucceed_afterTwoFailures() {
        stubFor(post(urlEqualTo("/updates"))
                .inScenario("retry-success")
                .whenScenarioStateIs(STARTED)
                .willReturn(aResponse().withStatus(500).withBody("{\"description\":\"error\"}"))
                .willSetStateTo("first-retry"));

        stubFor(post(urlEqualTo("/updates"))
                .inScenario("retry-success")
                .whenScenarioStateIs("first-retry")
                .willReturn(aResponse().withStatus(500).withBody("{\"description\":\"error\"}"))
                .willSetStateTo("second-retry"));

        stubFor(post(urlEqualTo("/updates"))
                .inScenario("retry-success")
                .whenScenarioStateIs("second-retry")
                .willReturn(aResponse().withStatus(200)));

        assertThatCode(() -> messageSender.send(
                        new LinkUpdateRequest(1, "https://github.com/user/repo", "коммит", List.of(1L))))
                .doesNotThrowAnyException();

        verify(3, postRequestedFor(urlEqualTo("/updates")));
    }

    @Test
    public void send_shouldNotRetry_whenServiceReturns400() {
        stubFor(post(urlEqualTo("/updates"))
                .willReturn(aResponse().withStatus(400).withBody("{\"description\":\"bad request\"}")));

        assertThatThrownBy(() -> messageSender.send(
                        new LinkUpdateRequest(1, "https://github.com/user/repo", "коммит", List.of(1L))))
                .isInstanceOf(BotClientException.class);

        verify(1, postRequestedFor(urlEqualTo("/updates")));
    }

    @Test
    public void send_shouldRespectRetryInterval() {
        stubFor(post(urlEqualTo("/updates"))
                .willReturn(aResponse().withStatus(500).withBody("{\"description\":\"error\"}")));

        long start = System.currentTimeMillis();

        assertThatCode(() -> messageSender.send(
                        new LinkUpdateRequest(1, "https://github.com/user/repo", "коммит", List.of(1L))))
                .doesNotThrowAnyException();

        long elapsed = System.currentTimeMillis() - start;

        assertThat(elapsed).isGreaterThanOrEqualTo(2000);
        verify(3, postRequestedFor(urlEqualTo("/updates")));
    }
}
