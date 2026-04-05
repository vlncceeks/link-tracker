package backend.academy.linktracker.scrapper.application.dto.response;

import backend.academy.linktracker.scrapper.application.dto.GitHubActor;
import backend.academy.linktracker.scrapper.application.dto.GitHubEventPayload;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record GitHubEventResponse(
    String id,
    String type,
    GitHubActor actor,
    GitHubEventPayload payload,
    @JsonProperty("created_at") Instant createdAt
) {}
