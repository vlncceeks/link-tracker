package backend.academy.linktracker.scrapper.application.client;

import backend.academy.linktracker.scrapper.application.dto.response.GitHubRepositoryResponse;
import java.util.Optional;

public interface GitHubClient {
    Optional<GitHubRepositoryResponse> fetchRepository(String owner, String repo);
}
