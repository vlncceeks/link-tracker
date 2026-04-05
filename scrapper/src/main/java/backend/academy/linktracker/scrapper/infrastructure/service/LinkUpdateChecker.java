package backend.academy.linktracker.scrapper.infrastructure.service;

import backend.academy.linktracker.scrapper.application.dto.response.GitHubEventResponse;
import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import java.util.List;
import java.util.Optional;

public interface LinkUpdateChecker {
    Optional<String> check(TrackedLink link);

    boolean supports(String url);
}
