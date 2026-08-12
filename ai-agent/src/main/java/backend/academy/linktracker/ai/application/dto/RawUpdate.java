package backend.academy.linktracker.ai.application.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RawUpdate(
        @NotNull Integer id,
        @NotNull String author,
        @NotNull String description,
        @NotNull List<Long> chatIds) {}
