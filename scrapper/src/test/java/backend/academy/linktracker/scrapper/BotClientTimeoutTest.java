package backend.academy.linktracker.scrapper;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import backend.academy.linktracker.scrapper.application.client.BotClientImpl.BotClientFactory;
import backend.academy.linktracker.scrapper.application.client.BotClientImpl.BotClientImpl;
import backend.academy.linktracker.scrapper.application.client.BotClientImpl.BotClientWrapper;
import backend.academy.linktracker.scrapper.application.dto.request.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.application.exception.BotClientException;
import backend.academy.linktracker.scrapper.infrastructure.configuration.BotProperties;
import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

@SpringBootTest
@ActiveProfiles("test")
@EnableWireMock
@TestPropertySource(
        properties = {
            "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration",
            "app.access-type=MEMORY"
        })
public class BotClientTimeoutTest {

    @Autowired
    private BotProperties properties;

    @InjectWireMock
    private WireMockServer wireMock;

    private BotClientWrapper botClient;

    @BeforeEach
    void setUp() {
        BotProperties testProperties = new BotProperties(
                "http://localhost:" + wireMock.port(), properties.connectTimeout(), properties.readTimeout());

        BotClientImpl client = new BotClientImpl(new BotClientFactory(testProperties));

        botClient = new BotClientWrapper(client);
    }

    @Test
    void send_shouldFailWithTimeout_whenServiceIsSlow() {
        stubFor(post(urlEqualTo("/updates"))
                .willReturn(aResponse().withStatus(200).withFixedDelay(6000)));

        LinkUpdateRequest request =
                new LinkUpdateRequest(1, "https://github.com/user/repo", "Новый коммит", List.of(1L));

        long start = System.currentTimeMillis();

        assertThatThrownBy(() -> botClient.send(request)).isInstanceOf(BotClientException.class);

        long elapsed = System.currentTimeMillis() - start;

        assertThat(elapsed).isLessThan(6000);
        assertThat(elapsed).isGreaterThanOrEqualTo(5000);
    }

    @Test
    void send_shouldSucceed_whenServiceRespondsInTime() {
        stubFor(post(urlEqualTo("/updates"))
                .willReturn(aResponse().withStatus(200).withFixedDelay(1000)));

        LinkUpdateRequest request =
                new LinkUpdateRequest(1, "https://github.com/user/repo", "Новый коммит", List.of(1L));

        assertThatCode(() -> botClient.send(request)).doesNotThrowAnyException();
    }
}
