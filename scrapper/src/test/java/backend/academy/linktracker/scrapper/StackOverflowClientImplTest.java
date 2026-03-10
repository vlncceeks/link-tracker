package backend.academy.linktracker.scrapper;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.properties.application.client.impl.StackOverflowClientImpl;
import backend.academy.linktracker.scrapper.properties.application.dto.response.StackOverflowResponse;
import backend.academy.linktracker.scrapper.properties.infrastructure.configuration.StackoverflowProperties;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class StackOverflowClientImplTest {
    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private StackOverflowClientImpl client;

    @BeforeEach
    void setUp() {
        StackoverflowProperties properties = new StackoverflowProperties();
        properties.setBaseUrl(wireMock.baseUrl());
        client = new StackOverflowClientImpl(properties);
    }

    @Test
    void fetchQuestion_validResponse_returnsItem() {
        wireMock.stubFor(get(urlPathEqualTo("/questions/12345"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    {
                      "items": [
                        {
                          "question_id": 12345,
                          "title": "How to use Java?",
                          "last_activity_date": 1700000000
                        }
                      ]
                    }
                    """)));

        Optional<StackOverflowResponse.StackOverflowItem> result = client.fetchQuestion(12345L);

        assertThat(result).isPresent();
        assertThat(result.get().questionId()).isEqualTo(12345L);
        assertThat(result.get().title()).isEqualTo("How to use Java?");
    }

    @Test
    void fetchQuestion_emptyItems_returnsEmpty() {
        wireMock.stubFor(get(urlPathEqualTo("/questions/12345"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    { "items": [] }
                    """)));

        Optional<StackOverflowResponse.StackOverflowItem> result = client.fetchQuestion(12345L);

        assertThat(result).isEmpty();
    }

    @Test
    void fetchQuestion_400_returnsEmpty() {
        wireMock.stubFor(
                get(urlPathEqualTo("/questions/12345")).willReturn(aResponse().withStatus(400)));

        Optional<StackOverflowResponse.StackOverflowItem> result = client.fetchQuestion(12345L);

        assertThat(result).isEmpty();
    }

    @Test
    void fetchQuestion_503_returnsEmpty() {
        wireMock.stubFor(
                get(urlPathEqualTo("/questions/12345")).willReturn(aResponse().withStatus(503)));

        Optional<StackOverflowResponse.StackOverflowItem> result = client.fetchQuestion(12345L);

        assertThat(result).isEmpty();
    }

    @Test
    void fetchQuestion_invalidJson_returnsEmpty() {
        wireMock.stubFor(get(urlPathEqualTo("/questions/12345"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("not-a-json")));

        Optional<StackOverflowResponse.StackOverflowItem> result = client.fetchQuestion(12345L);

        assertThat(result).isEmpty();
    }
}
