package backend.academy.linktracker.ai.infrastructure.service;

import backend.academy.linktracker.ai.application.dto.ProcessedUpdate;
import backend.academy.linktracker.ai.application.dto.RawUpdate;
import backend.academy.linktracker.ai.application.sender.MessageSender;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProcessUpdateService {
    private static final Logger logger = LoggerFactory.getLogger(ProcessUpdateService.class);
    private final FilteringService filteringService;
    private final SummarizationService summarizationService;
    private final GroupingService groupingService;
    private final MessageSender messageSender;

    public void processUpdate(RawUpdate rawUpdate) {
        if (!filteringService.isRelevant(rawUpdate)) {
            logger.atDebug().addKeyValue("id", rawUpdate.id()).log("Update filtered out");
            return;
        }

        String description = summarizationService.summarize(rawUpdate.description());
        RawUpdate summarizedRawUpdate =
                new RawUpdate(rawUpdate.id(), rawUpdate.author(), description, rawUpdate.chatIds());

        Map<Long, CompletableFuture<ProcessedUpdate>> futures = groupingService.group(summarizedRawUpdate);
        futures.values().forEach(future -> future.thenAccept(messageSender::send));
    }
}
