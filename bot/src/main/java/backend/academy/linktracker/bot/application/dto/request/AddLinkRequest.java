package backend.academy.linktracker.bot.application.dto.request;

import java.util.List;

public record AddLinkRequest(
    String url,
    List<String> tags,
    List<String> filters
) {}
