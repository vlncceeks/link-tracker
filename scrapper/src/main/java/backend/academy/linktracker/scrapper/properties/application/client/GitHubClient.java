package backend.academy.linktracker.scrapper.properties.application.client;

import backend.academy.linktracker.scrapper.properties.application.dto.response.GitHubRepositoryResponse;
import java.util.Optional;

public interface GitHubClient {
    Optional<GitHubRepositoryResponse> fetchRepository(String owner, String repo);
}
