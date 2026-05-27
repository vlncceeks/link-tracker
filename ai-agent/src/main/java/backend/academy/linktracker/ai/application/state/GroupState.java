package backend.academy.linktracker.ai.application.state;

import backend.academy.linktracker.ai.application.dto.ProcessedUpdate;
import backend.academy.linktracker.ai.application.dto.RawUpdate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public record GroupState(List<RawUpdate> updates, CompletableFuture<ProcessedUpdate> future) {}
