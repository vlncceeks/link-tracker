package backend.academy.linktracker.scrapper.application.client.impl;

import backend.academy.linktracker.scrapper.application.client.GitHubClient;
import backend.academy.linktracker.scrapper.application.dto.response.GitHubEventResponse;
import backend.academy.linktracker.scrapper.application.dto.response.GitHubRepositoryResponse;
import backend.academy.linktracker.scrapper.infrastructure.configuration.GithubProperties;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class GitHubClientImpl implements GitHubClient {
    private static final Logger logger = LoggerFactory.getLogger(GitHubClientImpl.class);
    private static final Pattern GITHUB_URL = Pattern.compile("https://github\\.com/([^/]+)/([^/]+)");

    private final RestClient restClient;

    public GitHubClientImpl(GithubProperties properties) {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("Accept", "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader("Authorization", "Bearer " + properties.getToken());

        this.restClient = builder.build();
    }

    @Override
    public Optional<GitHubRepositoryResponse> fetchRepository(String owner, String repo) {
        logger.atDebug().addKeyValue("owner", owner).addKeyValue("repo", repo).log("Запрос к Github API");
        try {
            GitHubRepositoryResponse response = restClient
                    .get()
                    .uri("/repos/{owner}/{repo}", owner, repo)
                    .retrieve()
                    .body(GitHubRepositoryResponse.class);
            return Optional.ofNullable(response);
        } catch (RestClientResponseException e) {
            logger.atWarn()
                    .addKeyValue("owner", owner)
                    .addKeyValue("repo", repo)
                    .addKeyValue("status", e.getStatusCode())
                    .log("Github API return Error");
            return Optional.empty();
        } catch (RestClientException e) {
            logger.atWarn()
                    .addKeyValue("owner", owner)
                    .addKeyValue("repo", repo)
                    .addKeyValue("error", e.getMessage())
                    .log("Error when receiving GitHub response");
            return Optional.empty();
        }
    }

    @Override
    public List<GitHubEventResponse> fetchEvents(String owner, String repo, Instant since) {
        logger.atDebug().addKeyValue("owner", owner).addKeyValue("repo", repo).log("Запрос к Github API");
        try {
            GitHubEventResponse[] response = restClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/repos/{owner}/{repo}/events")
                            .queryParam("per_page", 100)
                            .build(owner, repo))
                    .retrieve()
                    .body(GitHubEventResponse[].class);

            if (response == null) return List.of();

            return Arrays.stream(response)
                    .filter(e -> e.createdAt().isAfter(since))
                    .toList();
        } catch (RestClientResponseException e) {
            logger.atWarn()
                    .addKeyValue("owner", owner)
                    .addKeyValue("repo", repo)
                    .addKeyValue("status", e.getStatusCode())
                    .log("Github API return Error");
            return List.of();
        } catch (RestClientException e) {
            logger.atWarn()
                    .addKeyValue("owner", owner)
                    .addKeyValue("repo", repo)
                    .addKeyValue("error", e.getMessage())
                    .log("Error when receiving GitHub response");
            return List.of();
        }
    }

    public static Optional<String[]> parseUrl(String url) {
        Matcher matcher = GITHUB_URL.matcher(url);
        if (!matcher.matches()) return Optional.empty();
        return Optional.of(new String[] {matcher.group(1), matcher.group(2)});
    }
}
