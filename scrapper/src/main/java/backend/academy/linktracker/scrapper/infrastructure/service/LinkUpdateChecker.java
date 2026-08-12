package backend.academy.linktracker.scrapper.infrastructure.service;

import backend.academy.linktracker.scrapper.application.dto.InternalUpdateEvent;
import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import java.util.Optional;

public interface LinkUpdateChecker {
    Optional<InternalUpdateEvent> check(TrackedLink link);

    boolean supports(String url);
}
