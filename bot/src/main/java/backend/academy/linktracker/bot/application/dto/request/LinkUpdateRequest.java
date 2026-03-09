package backend.academy.linktracker.bot.application.dto.request;

import java.util.List;

public record LinkUpdateRequest(Long id, String url, String description, List<Long> tgChatIds) {}
