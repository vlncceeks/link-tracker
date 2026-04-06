package backend.academy.linktracker.scrapper.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GitHubIssue(
        Long number,
        String title,
        String state,
        @JsonProperty("html_url") String htmlUrl) {}
