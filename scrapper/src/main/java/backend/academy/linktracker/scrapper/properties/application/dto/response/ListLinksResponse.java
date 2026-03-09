package backend.academy.linktracker.scrapper.properties.application.dto.response;

import java.util.List;

public record ListLinksResponse(List<LinkResponse> links, Integer size) {}
