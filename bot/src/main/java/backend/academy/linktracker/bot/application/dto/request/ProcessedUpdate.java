package backend.academy.linktracker.bot.application.dto.request;

import backend.academy.linktracker.bot.application.state.Priority;
import java.util.List;

public record ProcessedUpdate(int id, String description, List<Long> chatIds, Priority priority) {}
