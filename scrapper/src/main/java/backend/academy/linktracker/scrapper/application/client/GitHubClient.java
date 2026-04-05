package backend.academy.linktracker.scrapper.application.client;

import backend.academy.linktracker.scrapper.application.dto.response.GitHubEventResponse;
import backend.academy.linktracker.scrapper.application.dto.response.GitHubRepositoryResponse;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface GitHubClient {
    Optional<GitHubRepositoryResponse> fetchRepository(String owner, String repo);
    List<GitHubEventResponse> fetchEvents(String owner, String repo, Instant since);
}
