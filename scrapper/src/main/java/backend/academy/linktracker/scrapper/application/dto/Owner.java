package backend.academy.linktracker.scrapper.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Owner(@JsonProperty("display_name") String displayName) {}
