package backend.academy.linktracker.scrapper.properties.application.client;

import backend.academy.linktracker.scrapper.properties.application.dto.response.StackOverflowResponse;
import java.util.Optional;

public interface StackOverflowClient {
    Optional<StackOverflowResponse.StackOverflowItem> fetchQuestion(Long questionId);
}
