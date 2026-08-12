package backend.academy.linktracker.scrapper.application.client.GitHubClientImpl;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Getter
public class GitHubClientImpl {
    private final RestClient restClient;

    public GitHubClientImpl(GitHubClientFactory clientFactory) {
        this.restClient = clientFactory.createRestClient();
    }
}
