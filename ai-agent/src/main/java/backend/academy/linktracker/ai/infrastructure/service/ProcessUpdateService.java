package backend.academy.linktracker.ai.infrastructure.service;

import backend.academy.linktracker.ai.application.dto.ProcessedUpdate;
import backend.academy.linktracker.ai.application.dto.RawUpdate;
import backend.academy.linktracker.ai.application.exception.UpdateNonRelevantException;
import backend.academy.linktracker.ai.application.state.Priority;
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

    public ProcessedUpdate processUpdate(RawUpdate rawUpdate) {
        if (!filteringService.isRelevant(rawUpdate))
            throw new UpdateNonRelevantException("Это обновление было отфильтровано: " + rawUpdate.id());

        String description = summarizationService.summarize(rawUpdate.description());

        return new ProcessedUpdate(rawUpdate.id(), description, rawUpdate.chatIds(), Priority.MEDIUM);
    }
}
