package backend.academy.linktracker.ai.infrastructure.service;

import backend.academy.linktracker.ai.application.dto.RawUpdate;
import backend.academy.linktracker.ai.application.state.Priority;
import backend.academy.linktracker.ai.infrastructure.properties.PrioritizationProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrioritizationService {
    private static final Logger logger = LoggerFactory.getLogger(PrioritizationService.class);
    private final PrioritizationProperties properties;

    public Priority prioritize(RawUpdate rawUpdate) {
        logger.atInfo().addKeyValue("id", rawUpdate.id()).log("PrioritizationService started");

        List<String> highKeywords = properties.highKeywords();
        List<String> lowKeywords = properties.lowKeywords();

        String description = rawUpdate.description();

        for (String highKeyword : highKeywords) {
            if (description.contains(highKeyword)) {
                logger.atInfo().addKeyValue("id", rawUpdate.id()).log("Update has High priority");
                return Priority.HIGH;
            }
        }

        for (String lowKeyword : lowKeywords) {
            if (description.contains(lowKeyword)) {
                logger.atInfo().addKeyValue("id", rawUpdate.id()).log("Update has Low priority");
                return Priority.LOW;
            }
        }

        logger.atInfo().addKeyValue("id", rawUpdate.id()).log("Update has Medium priority");
        return Priority.MEDIUM;
    }
}
