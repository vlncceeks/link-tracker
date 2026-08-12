package backend.academy.linktracker.scrapper.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubEventPayload(
        String action,
        @JsonProperty("pull_request") GitHubPullRequest pullRequest,
        GitHubIssue issue,
        String ref,
        String head,
        String before,
        @JsonProperty("push_id") Long pushId) {}
