package backend.academy.linktracker.scrapper.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubPullRequest(
    Long number,
    String state,
    @JsonProperty("html_url") String htmlUrl,
    GitHubBranch head
) {}
