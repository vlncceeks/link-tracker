package backend.academy.linktracker.scrapper.application.client.GitHubClientImpl;

import backend.academy.linktracker.scrapper.application.dto.response.GitHubEventResponse;
import backend.academy.linktracker.scrapper.application.dto.response.GitHubRepositoryResponse;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class GitHubClientWrapper implements backend.academy.linktracker.scrapper.application.client.GitHubClient {
    private static final Logger logger = LoggerFactory.getLogger(GitHubClientWrapper.class);
    private final GitHubClientImpl gitHubClient;

    @Override
    public Optional<GitHubRepositoryResponse> fetchRepository(String owner, String repo) {
        logger.atDebug().addKeyValue("owner", owner).addKeyValue("repo", repo).log("Запрос к Github API");
        try {
            GitHubRepositoryResponse response = gitHubClient
                    .getRestClient()
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
            GitHubEventResponse[] response = gitHubClient
                    .getRestClient()
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
}
