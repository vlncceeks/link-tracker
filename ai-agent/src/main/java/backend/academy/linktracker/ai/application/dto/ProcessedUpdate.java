package backend.academy.linktracker.ai.application.dto;

import backend.academy.linktracker.ai.application.state.Priority;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ProcessedUpdate(
        @NotNull Integer id,
        @NotNull String description,
        @NotNull List<Long> chatIds,
        @NotNull Priority priority) {}
