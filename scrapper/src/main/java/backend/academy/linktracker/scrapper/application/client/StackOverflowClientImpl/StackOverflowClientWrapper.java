package backend.academy.linktracker.scrapper.application.client.StackOverflowClientImpl;

import backend.academy.linktracker.scrapper.application.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.application.dto.response.StackOverflowAnswerResponse;
import backend.academy.linktracker.scrapper.application.dto.response.StackOverflowCommentResponse;
import backend.academy.linktracker.scrapper.application.dto.response.StackOverflowResponse;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class StackOverflowClientWrapper implements StackOverflowClient {
    private static final Logger logger = LoggerFactory.getLogger(StackOverflowClientWrapper.class);
    private static final Pattern SO_URL = Pattern.compile("https://stackoverflow\\.com/questions/(\\d+).*");

    private final StackOverflowClientImpl stackOverflowClient;

    @Override
    public Optional<StackOverflowResponse.StackOverflowItem> fetchQuestion(Long questionId) {
        logger.atDebug().addKeyValue("questionId", questionId).log("Запрос к StackOverflow API");
        try {
            StackOverflowResponse response = stackOverflowClient
                    .getRestClient()
                    .get()
                    .uri("/questions/{id}?site=stackoverflow&filter=!nNPvSNdWme", questionId)
                    .retrieve()
                    .body(StackOverflowResponse.class);

            if (response == null || response.items().isEmpty()) return Optional.empty();
            return Optional.of(response.items().getFirst());
        } catch (RestClientResponseException e) {
            logger.atWarn()
                    .addKeyValue("questionId", questionId)
                    .addKeyValue("status", e.getStatusCode())
                    .log("StackOverflow API return Error");
            return Optional.empty();
        } catch (RestClientException e) {
            logger.atWarn()
                    .addKeyValue("questionId", questionId)
                    .addKeyValue("error", e.getMessage())
                    .log("Error when receiving StackOverflow response");
            return Optional.empty();
        }
    }

    public List<StackOverflowAnswerResponse.AnswerItem> fetchAnswers(Long questionId, Instant since) {
        try {
            StackOverflowAnswerResponse response = stackOverflowClient
                    .getRestClient()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/questions/{id}/answers")
                            .queryParam("site", "stackoverflow")
                            .queryParam("filter", "withbody")
                            .queryParam("sort", "creation")
                            .queryParam("order", "desc")
                            .build(questionId))
                    .retrieve()
                    .body(StackOverflowAnswerResponse.class);

            if (response == null) return List.of();

            return response.items().stream()
                    .filter(a -> a.creationDate().isAfter(since))
                    .toList();
        } catch (RestClientException e) {
            logger.atWarn().addKeyValue("questionId", questionId).log("Ошибка получения ответов SO");
            return List.of();
        }
    }

    public List<StackOverflowCommentResponse.CommentItem> fetchComments(Long questionId, Instant since) {
        try {
            StackOverflowCommentResponse response = stackOverflowClient
                    .getRestClient()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/questions/{id}/answers")
                            .queryParam("filter", "withbody")
                            .queryParam("sort", "creation")
                            .queryParam("order", "desc")
                            .build(questionId))
                    .retrieve()
                    .body(StackOverflowCommentResponse.class);

            if (response == null) return List.of();

            return response.items().stream()
                    .filter(a -> a.creationDate().isAfter(since))
                    .toList();
        } catch (RestClientException e) {
            logger.atWarn().addKeyValue("questionId", questionId).log("Ошибка получения ответов SO");
            return List.of();
        }
    }

    public static Optional<Long> parseUrl(String url) {
        Matcher matcher = SO_URL.matcher(url);
        if (!matcher.matches()) return Optional.empty();
        return Optional.of(Long.parseLong(matcher.group(1)));
    }
}
