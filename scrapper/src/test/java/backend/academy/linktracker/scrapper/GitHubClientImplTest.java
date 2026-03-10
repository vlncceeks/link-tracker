package backend.academy.linktracker.scrapper;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.properties.application.client.impl.GitHubClientImpl;
import backend.academy.linktracker.scrapper.properties.application.dto.response.GitHubRepositoryResponse;
import backend.academy.linktracker.scrapper.properties.infrastructure.configuration.GithubProperties;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class GitHubClientImplTest {
    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private GitHubClientImpl client;

    @BeforeEach
    void setUp() {
        GithubProperties properties = new GithubProperties();
        properties.setBaseUrl(wireMock.baseUrl());
        properties.setToken("test-token");
        client = new GitHubClientImpl(properties);
    }

    @Test
    void fetchRepository_validResponse_returnsData() {
        wireMock.stubFor(get(urlPathEqualTo("/repos/user/repo"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                    {
                      "id": 1,
                      "name": "repo",
                      "full_name": "user/repo",
                      "pushed_at": "2024-01-01T00:00:00Z",
                      "updated_at": "2024-01-01T00:00:00Z"
                    }
                    """)));

        Optional<GitHubRepositoryResponse> result = client.fetchRepository("user", "repo");

        assertThat(result).isPresent();
        assertThat(result.get().fullName()).isEqualTo("user/repo");
    }

    @Test
    void fetchRepository_404_returnsEmpty() {
        wireMock.stubFor(
                get(urlPathEqualTo("/repos/user/repo")).willReturn(aResponse().withStatus(404)));

        Optional<GitHubRepositoryResponse> result = client.fetchRepository("user", "repo");

        assertThat(result).isEmpty();
    }

    @Test
    void fetchRepository_500_returnsEmpty() {
        wireMock.stubFor(
                get(urlPathEqualTo("/repos/user/repo")).willReturn(aResponse().withStatus(500)));

        Optional<GitHubRepositoryResponse> result = client.fetchRepository("user", "repo");

        assertThat(result).isEmpty();
    }

    @Test
    void fetchRepository_invalidJson_returnsEmpty() {
        wireMock.stubFor(get(urlPathEqualTo("/repos/user/repo"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("not-a-json")));

        Optional<GitHubRepositoryResponse> result = client.fetchRepository("user", "repo");

        assertThat(result).isEmpty();
    }
}
