package backend.academy.linktracker.ai.infrastructure.service;

import backend.academy.linktracker.ai.application.dto.ProcessedUpdate;
import backend.academy.linktracker.ai.application.dto.RawUpdate;
import backend.academy.linktracker.ai.application.exception.UpdateNonRelevantException;
import backend.academy.linktracker.ai.application.sender.MessageSender;
import backend.academy.linktracker.ai.application.state.Priority;
import backend.academy.linktracker.ai.infrastructure.properties.GroupingProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProcessUpdateService {
    private static final Logger logger = LoggerFactory.getLogger(ProcessUpdateService.class);
    private final FilteringService filteringService;
    private final SummarizationService summarizationService;
    private final GroupingService groupingService;
    private final MessageSender messageSender;

    public void processUpdate(RawUpdate rawUpdate) {
        if (!filteringService.isRelevant(rawUpdate)){
            logger.atDebug()
                .addKeyValue("id", rawUpdate.id())
                .log("Update filtered out");
            return;
        }

        String description = summarizationService.summarize(rawUpdate.description());
        RawUpdate summarizedRawUpdate = new RawUpdate(rawUpdate.id(), rawUpdate.author(), description, rawUpdate.chatIds());

        Map<Long, CompletableFuture<ProcessedUpdate>> futures = groupingService.group(summarizedRawUpdate);
        futures.values().forEach(future -> future.thenAccept(messageSender::send));
    }

}
