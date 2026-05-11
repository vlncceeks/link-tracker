package backend.academy.linktracker.bot;

import backend.academy.linktracker.bot.application.client.impl.ScrapperClientWrapper;
import backend.academy.linktracker.bot.application.dto.response.ListLinksResponse;
import backend.academy.linktracker.bot.application.exception.RetryableException;
import backend.academy.linktracker.bot.application.exception.ScrapperClientException;
import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@EnableWireMock(@ConfigureWireMock(port = 8089))
@TestPropertySource(properties = "scrapper.base-url=http://localhost:8089")
class ScrapperRetryTest {

    @InjectWireMock
    private WireMockServer wireMock;

    @Autowired
    private ScrapperClientWrapper scrapperClient;

    @Test
    public void getLinks_shouldNotRetry_whenServiceReturns200() {
        stubFor(get(urlEqualTo("/links"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("{\"links\":[]}")));

        scrapperClient.getLinks(1L);

        verify(1, getRequestedFor(urlEqualTo("/links")));
    }

    @Test
    public void getLinks_shouldRetryThreeTimes_whenServiceReturns500() {
        stubFor(get(urlEqualTo("/links"))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("{\"description\":\"error\"}")));

        assertThatThrownBy(() -> scrapperClient.getLinks(1L))
            .isInstanceOf(RetryableException.class)
            .hasMessageContaining("500");

        verify(3, getRequestedFor(urlEqualTo("/links")));
    }

    @Test
    public void testRetrySucceedsAfterTwoFailures() {
        stubFor(get(urlEqualTo("/links"))
            .inScenario("retry-success")
            .whenScenarioStateIs(STARTED)
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("{\"description\":\"error\"}"))
            .willSetStateTo("first-retry"));

        stubFor(get(urlEqualTo("/links"))
            .inScenario("retry-success")
            .whenScenarioStateIs("first-retry")
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("{\"description\":\"error\"}"))
            .willSetStateTo("second-retry"));

        stubFor(get(urlEqualTo("/links"))
            .inScenario("retry-success")
            .whenScenarioStateIs("second-retry")
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("{\"links\":[]}")));

        ListLinksResponse response = scrapperClient.getLinks(1L);

        assertThat(response).isNotNull();
        verify(3, getRequestedFor(urlEqualTo("/links")));
    }

    @Test
    public void testNoRetryOn4xx() {
        stubFor(get(urlEqualTo("/links"))
            .willReturn(aResponse()
                .withStatus(400)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("{\"description\":\"bad request\"}")));

        assertThatThrownBy(() -> scrapperClient.getLinks(1L))
            .isInstanceOf(ScrapperClientException.class);

        verify(1, getRequestedFor(urlEqualTo("/links")));
    }

    @Test
    public void testRetryInterval() {
        stubFor(get(urlEqualTo("/links"))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .withBody("{\"description\":\"error\"}")));

        long start = System.currentTimeMillis();

        assertThatThrownBy(() -> scrapperClient.getLinks(1L))
            .isInstanceOf(RetryableException.class);

        long elapsed = System.currentTimeMillis() - start;

        assertThat(elapsed).isGreaterThanOrEqualTo(2000);
        verify(3, getRequestedFor(urlEqualTo("/links")));
    }
}
