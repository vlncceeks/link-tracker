package backend.academy.linktracker.scrapper.application.client.impl;

import backend.academy.linktracker.scrapper.application.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.application.dto.response.StackOverflowResponse;
import backend.academy.linktracker.scrapper.infrastructure.configuration.StackoverflowProperties;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class StackOverflowClientImpl implements StackOverflowClient {
    private static final Logger logger = LoggerFactory.getLogger(StackOverflowClientImpl.class);
    private static final Pattern SO_URL = Pattern.compile("https://stackoverflow\\.com/questions/(\\d+).*");

    private final RestClient restClient;

    public StackOverflowClientImpl(StackoverflowProperties properties) {
        this.restClient = RestClient.builder().baseUrl(properties.getBaseUrl()).build();
    }

    @Override
    public Optional<StackOverflowResponse.StackOverflowItem> fetchQuestion(Long questionId) {
        logger.atDebug().addKeyValue("questionId", questionId).log("Запрос к StackOverflow API");
        try {
            StackOverflowResponse response = restClient
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
                    .log("StackOverflow API вернул ошибку");
            return Optional.empty();
        } catch (RestClientException e) {
            logger.atWarn()
                    .addKeyValue("questionId", questionId)
                    .addKeyValue("error", e.getMessage())
                    .log("Ошибка при обработке ответа StackOverflow");
            return Optional.empty();
        }
    }

    public static Optional<Long> parseUrl(String url) {
        Matcher matcher = SO_URL.matcher(url);
        if (!matcher.matches()) return Optional.empty();
        return Optional.of(Long.parseLong(matcher.group(1)));
    }
}
