package backend.academy.linktracker.scrapper.properties.application.service;

import backend.academy.linktracker.scrapper.properties.application.link.TrackedLink;
import java.util.Optional;

public interface LinkUpdateChecker {
    Optional<String> check(TrackedLink link);

    boolean supports(String url);
}
