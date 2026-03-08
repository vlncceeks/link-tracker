package backend.academy.linktracker.scrapper.properties.application.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record GitHubRepositoryResponse(
    Long id,
    String name,

    @JsonProperty("full_name")
    String fullName,

    @JsonProperty("pushed_at")
    Instant pushedAt,

    @JsonProperty("updated_at")
    Instant updatedAt
) {
}
