package backend.academy.linktracker.scrapper.application.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record LinkUpdateRequest(
        @NotNull Integer id,
        @NotNull String url,
        String description,
        @NotNull List<Long> tgChatIds) {}
