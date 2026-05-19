package backend.academy.linktracker.scrapper.application.dto.response;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RawUpdate(
        @NotNull Integer id,
        @NotNull String description,
        @NotNull List<Long> chatIds) {}
