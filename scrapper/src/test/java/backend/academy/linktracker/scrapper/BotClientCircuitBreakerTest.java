package backend.academy.linktracker.scrapper;

import backend.academy.linktracker.scrapper.application.client.MessageSender;
import backend.academy.linktracker.scrapper.application.dto.request.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.infrastructure.configuration.BotProperties;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import java.time.Duration;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@EnableWireMock(@ConfigureWireMock(port = 8089))
@TestPropertySource(properties = {
    "bot.base-url=http://localhost:8089",
    "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration",
    "app.access-type=MEMORY",
    "app.message-sender-type=DIRECTLY"
})
class BotClientCircuitBreakerTest {

    @Autowired
    private BotProperties properties;

    @InjectWireMock
    private WireMockServer wireMock;

    @Autowired
    private MessageSender botClient;

    private CircuitBreaker circuitBreaker;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    int numberOfCalls;

    @Value("${resilience4j.circuitbreaker.instances.botCB.wait-duration-in-open-state:1s}")
    Duration waitDurationInOpenState;

    int permittedCallsInHalfOpenState;
    LinkUpdateRequest request;

    @BeforeEach
    void setUp() {
        circuitBreaker = circuitBreakerRegistry.circuitBreaker("botCB");
        circuitBreaker.reset();
        numberOfCalls = circuitBreaker.getCircuitBreakerConfig().getMinimumNumberOfCalls();
        permittedCallsInHalfOpenState = circuitBreaker.getCircuitBreakerConfig().getPermittedNumberOfCallsInHalfOpenState();
        request = new LinkUpdateRequest(
            1, "https://github.com/user/repo", "Новый коммит", List.of(1L)
        );
    }

    @Test
    void shouldTransitionToOpen_whenFailureThresholdReached() {
        stubFor(post(urlEqualTo("/updates"))
            .willReturn(aResponse().withStatus(500)));

        for (int i = 0; i < numberOfCalls; i++) {
            assertThatThrownBy(() -> botClient.send(request))
                .isInstanceOf(Exception.class);
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        wireMock.resetRequests();

        long startTime = System.nanoTime();
        assertThatThrownBy(() -> botClient.send(request))
            .isInstanceOf(CallNotPermittedException.class);
        long duration = System.nanoTime() - startTime;

        assertThat(duration).isLessThan(10_000_000);
        verify(0, postRequestedFor(urlEqualTo("/updates")));
    }

    @Test
    void shouldTransitionToClosed_whenHalfOpenCallsSucceed() throws Exception {
        stubFor(post(urlEqualTo("/updates"))
            .willReturn(aResponse().withStatus(500)));

        for (int i = 0; i < numberOfCalls; i++) {
            assertThatThrownBy(() -> botClient.send(request))
                .isInstanceOf(Exception.class);
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        Thread.sleep(waitDurationInOpenState.toMillis() + 500);

        stubFor(post(urlEqualTo("/updates"))
            .willReturn(aResponse().withStatus(200)));

        for (int i = 0; i < permittedCallsInHalfOpenState; i++) {
            assertThatCode(() -> botClient.send(request))
                .doesNotThrowAnyException();
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }

    @Test
    void shouldReturnToOpen_whenHalfOpenCallsFail() throws Exception {
        stubFor(post(urlEqualTo("/updates"))
            .willReturn(aResponse().withStatus(500)));

        for (int i = 0; i < numberOfCalls; i++) {
            assertThatThrownBy(() -> botClient.send(request))
                .isInstanceOf(Exception.class);
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);

        Thread.sleep(waitDurationInOpenState.toMillis() + 500);

        for (int i = 0; i < permittedCallsInHalfOpenState; i++) {
            assertThatThrownBy(() -> botClient.send(request))  // ← исправлено
                .isInstanceOf(Exception.class);
        }

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }
}
