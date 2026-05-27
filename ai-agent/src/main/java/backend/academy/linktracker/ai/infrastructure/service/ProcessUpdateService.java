package backend.academy.linktracker.ai.infrastructure.service;

import backend.academy.linktracker.ai.application.dto.ProcessedUpdate;
import backend.academy.linktracker.ai.application.dto.RawUpdate;
import backend.academy.linktracker.ai.application.exception.UpdateNonRelevantException;
import backend.academy.linktracker.ai.application.state.Priority;
import backend.academy.linktracker.ai.infrastructure.properties.GroupingProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProcessUpdateService {
    private static final Logger logger = LoggerFactory.getLogger(ProcessUpdateService.class);
    private final GroupingProperties properties;
    private final FilteringService filteringService;
    private final SummarizationService summarizationService;
    private final GroupingService groupingService;

    private final Map<Long, List<RawUpdate>> updates = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler =
        Executors.newScheduledThreadPool(1);

    public ProcessedUpdate processUpdate(RawUpdate rawUpdate) {
        if (!filteringService.isRelevant(rawUpdate))
            throw new UpdateNonRelevantException("Это обновление было отфильтровано: " + rawUpdate.id());

        String description = summarizationService.summarize(rawUpdate.description());

        List<Long> chatIds = rawUpdate.chatIds();
        for (Long chatId : chatIds) {
            if (updates.containsKey(chatId)) {
                updates.get(chatId).add(rawUpdate);
            }
            else {
                updates.put(chatId, List.of(rawUpdate));
                scheduler.schedule(
                    () -> flush(chatId),
                    properties.windowMs(),
                    TimeUnit.MILLISECONDS
                );
            }
        }

        return new ProcessedUpdate(rawUpdate.id(), description, rawUpdate.chatIds(), Priority.MEDIUM);
    }

    private RawUpdate flush(long chatId) {
        List<RawUpdate> rawUpdates = updates.remove(chatId);

        return null;
    }
}
