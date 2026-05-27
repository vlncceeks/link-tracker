package backend.academy.linktracker.ai.infrastructure.service;

import backend.academy.linktracker.ai.application.dto.ProcessedUpdate;
import backend.academy.linktracker.ai.application.dto.RawUpdate;
import backend.academy.linktracker.ai.application.state.GroupState;
import backend.academy.linktracker.ai.application.state.Priority;
import backend.academy.linktracker.ai.infrastructure.properties.GroupingProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class GroupingService {
    private static final Logger logger = LoggerFactory.getLogger(GroupingService.class);
    private final PrioritizationService prioritizationService;
    private final GroupingProperties properties;
    private final Map<Long, GroupState> groups = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler =
        Executors.newScheduledThreadPool(1);

    public Map<Long, CompletableFuture<ProcessedUpdate>> group(RawUpdate update) {
        logger.atInfo().addKeyValue("id", update.id()).log("Grouping started");

        Map<Long, CompletableFuture<ProcessedUpdate>> result = new HashMap<>();

        for (Long chatId : update.chatIds()) {
            groups.compute(chatId, (id, state) -> {
                if (state == null) {
                    List<RawUpdate> updates =
                        Collections.synchronizedList(
                            new ArrayList<>()
                        );
                    updates.add(update);

                    CompletableFuture<ProcessedUpdate> future = new CompletableFuture<>();

                    GroupState newState =
                        new GroupState(updates, future);

                    scheduler.schedule(
                        () -> flush(id),
                        properties.windowMs(),
                        TimeUnit.MILLISECONDS
                    );

                    result.put(chatId, future);
                    return newState;
                }
                state.updates().add(update);
                result.put(id, state.future());
                return state;
            });
        }

        return result;
    }

    private void flush(Long chatId) {
        GroupState state = groups.remove(chatId);
        if (state == null) return;

        List<RawUpdate> updates = state.updates();
        if (updates == null || updates.isEmpty()) return;

        ProcessedUpdate result = groupMessage(chatId, updates);
        state.future().complete(result);
    }

    private ProcessedUpdate groupMessage(Long chatId, List<RawUpdate> updates) {
        int id = updates.get(0).id();
        if (updates.size() == 1)
            return new ProcessedUpdate(
                id,
                updates.get(0).description(),
                List.of(chatId),
                prioritizationService.prioritize(updates.get(0).description())
            );

        StringBuilder description = new StringBuilder();
        Priority maxPriority = Priority.LOW;
        int counter = 1;

        for (RawUpdate update : updates) {
            if (maxPriority != Priority.HIGH) {
                Priority priority = prioritizationService.prioritize(update.description());
                maxPriority = findMaxPriority(priority, maxPriority);
            }

            description.append(counter + ". " + update.description() + "\n");
            counter++;
        }

        logger.atInfo().addKeyValue("id", id).log("Grouping finish");
        return new ProcessedUpdate(id, description.toString(), List.of(chatId), maxPriority);
    }

    private Priority findMaxPriority(Priority priority1, Priority priority2) {
        if (priority1 == Priority.HIGH || priority2 == Priority.HIGH) return Priority.HIGH;
        if (priority1 == Priority.MEDIUM || priority2 == Priority.MEDIUM) return Priority.MEDIUM;
        return Priority.LOW;
    }
}
