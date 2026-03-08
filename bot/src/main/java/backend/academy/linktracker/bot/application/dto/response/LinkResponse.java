package backend.academy.linktracker.bot.application.dto.response;

import java.util.List;

public record LinkResponse(
    Integer id,
    String url,
    List<String> tags,
    List<String> filters
) {}
