package backend.academy.linktracker.scrapper.application.dto.response;

import java.util.List;

public record LinkResponse(Integer id, String url, List<String> tags, List<String> filters) {}
