package backend.academy.linktracker.bot;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import backend.academy.linktracker.bot.application.client.impl.ScrapperClientImpl;
import backend.academy.linktracker.bot.application.client.impl.ScrapperClientWrapper;
import backend.academy.linktracker.bot.application.client.impl.ScrapperFactory;
import backend.academy.linktracker.bot.infrastructure.configuration.ScrapperProperties;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("test")
@EnableWireMock
class ScrapperClientTimeoutTest {

    @Autowired
    private ScrapperProperties properties;

    @InjectWireMock
    private WireMockServer wireMock;

    private ScrapperClientWrapper scrapperClient;

    @BeforeEach
    void setUp() {
        ScrapperProperties testProperties = new ScrapperProperties(
                "http://localhost:" + wireMock.port(), properties.connectTimeout(), properties.readTimeout());
        ScrapperClientImpl client = new ScrapperClientImpl(new ScrapperFactory(), testProperties, new ObjectMapper());
        scrapperClient = new ScrapperClientWrapper(client);
    }

    @Test
    void getLinks_shouldFailWithTimeout_whenServiceIsSlow() {
        stubFor(get(urlEqualTo("/links"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"links\":[]}")
                        .withFixedDelay(6000)));

        long start = System.currentTimeMillis();

        assertThatThrownBy(() -> scrapperClient.getLinks(1L)).isInstanceOf(Exception.class);

        long elapsed = System.currentTimeMillis() - start;

        assertThat(elapsed).isLessThan(6000);
        assertThat(elapsed).isGreaterThanOrEqualTo(5000);
    }

    @Test
    void getLinks_shouldSucceed_whenServiceRespondsInTime() {
        stubFor(get(urlEqualTo("/links"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"links\":[]}")
                        .withFixedDelay(1000)));

        assertThatCode(() -> scrapperClient.getLinks(1L)).doesNotThrowAnyException();
    }
}
