package backend.academy.linktracker.ai.infrastructure.service;

import backend.academy.linktracker.ai.infrastructure.properties.SummarizationProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SummarizationService {
    private static final Logger logger = LoggerFactory.getLogger(SummarizationService.class);
    private final SummarizationProperties properties;

    public String summarize(String description) {
        if (description.length() < properties.threshold()) {
            logger.info("Summarization is not necessary");
            return description;
        }

        logger.info("Summarization started");

        return description.substring(0, properties.threshold());
    }
}
