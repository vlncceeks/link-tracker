package backend.academy.linktracker.ai.infrastructure.service;

import backend.academy.linktracker.ai.application.state.Priority;
import backend.academy.linktracker.ai.infrastructure.properties.PrioritizationProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrioritizationService {
    private static final Logger logger = LoggerFactory.getLogger(PrioritizationService.class);
    private final PrioritizationProperties properties;

    public Priority prioritize(String description) {
        logger.atInfo().log("PrioritizationService started");

        List<String> highKeywords = properties.highKeywords();
        List<String> lowKeywords = properties.lowKeywords();

        for (String highKeyword : highKeywords) {
            if (StringUtils.containsIgnoreCase(description, highKeyword)) {
                logger.atInfo().log("Update has High priority");
                return Priority.HIGH;
            }
        }

        for (String lowKeyword : lowKeywords) {
            if (StringUtils.containsIgnoreCase(description, lowKeyword)) {
                logger.atInfo().log("Update has Low priority");
                return Priority.LOW;
            }
        }

        logger.atInfo().log("Update has Medium priority");
        return Priority.MEDIUM;
    }
}
