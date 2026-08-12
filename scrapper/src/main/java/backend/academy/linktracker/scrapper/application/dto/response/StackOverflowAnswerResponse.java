package backend.academy.linktracker.scrapper.application.dto.response;

import backend.academy.linktracker.scrapper.application.dto.Owner;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public record StackOverflowAnswerResponse(
        @JsonProperty("items") List<AnswerItem> items) {
    public record AnswerItem(
            @JsonProperty("answer_id") Long answerId,
            @JsonProperty("owner") Owner owner,
            @JsonProperty("creation_date") Instant creationDate,
            @JsonProperty("body_markdown") String body) {}
}
