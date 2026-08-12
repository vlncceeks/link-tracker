package backend.academy.linktracker.scrapper.application.client.GitHubClientImpl;

import backend.academy.linktracker.scrapper.infrastructure.configuration.GithubProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class GitHubClientFactory {
    private final GithubProperties properties;

    public RestClient createRestClient() {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader("Authorization", "Bearer " + properties.getToken())
                .build();
    }
}
