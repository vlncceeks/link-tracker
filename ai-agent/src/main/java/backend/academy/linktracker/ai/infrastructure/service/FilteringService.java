package backend.academy.linktracker.ai.infrastructure.service;

import backend.academy.linktracker.ai.application.dto.RawUpdate;
import backend.academy.linktracker.ai.infrastructure.properties.FilteringProperties;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FilteringService {
    private static final Logger logger = LoggerFactory.getLogger(FilteringService.class);
    private final FilteringProperties properties;

    public boolean isRelevant(RawUpdate rawUpdate) {
        logger.atInfo().addKeyValue("id", rawUpdate.id()).log("Filtering update");

        String description = rawUpdate.description();

        if (description.length() < properties.minLength()) {
            logger.atInfo().addKeyValue("id", rawUpdate.id()).log("Update is not relevant");
            return false;
        }

        List<String> stopWords = properties.stopWords();

        for (String stopWord : stopWords) {
            if (description.contains(stopWord)) {
                logger.atInfo().addKeyValue("id", rawUpdate.id()).log("Update is not relevant");
                return false;
            }
        }

        List<String> excludedAuthors = properties.excludedAuthors();

        for (String excludedAuthor : excludedAuthors) {
            if (description.contains(excludedAuthor)) {
                logger.atInfo().addKeyValue("id", rawUpdate.id()).log("Update is not relevant");
                return false;
            }
        }

        logger.atInfo().addKeyValue("id", rawUpdate.id()).log("Update is relevant");
        return true;
    }
}
