package backend.academy.linktracker.scrapper.properties.application.dto.request;

import java.util.List;

public record AddLinkRequest(String url, List<String> tags, List<String> filters) {}
