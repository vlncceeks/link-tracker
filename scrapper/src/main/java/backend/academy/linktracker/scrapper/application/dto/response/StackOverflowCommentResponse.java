package backend.academy.linktracker.scrapper.application.dto.response;

import backend.academy.linktracker.scrapper.application.dto.Owner;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;

public record StackOverflowCommentResponse(
        @JsonProperty("items") List<CommentItem> items) {
    public record CommentItem(
            @JsonProperty("comment_id") Long commentId,
            @JsonProperty("owner") Owner owner,
            @JsonProperty("creation_date") Instant creationDate,
            @JsonProperty("body_markdown") String body) {}
}
