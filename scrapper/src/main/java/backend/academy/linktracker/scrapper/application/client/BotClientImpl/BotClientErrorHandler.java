package backend.academy.linktracker.scrapper.application.client.BotClientImpl;

import backend.academy.linktracker.scrapper.application.exception.BotClientException;
import backend.academy.linktracker.scrapper.application.exception.RetryableException;
import java.io.IOException;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;

@RequiredArgsConstructor
public class BotClientErrorHandler implements RestClient.ResponseSpec.ErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(BotClientErrorHandler.class);
    private final Set<HttpStatusCode> retryableStatuses;

    @Override
    public void handle(HttpRequest request, ClientHttpResponse resp) throws IOException {
        HttpStatusCode status = resp.getStatusCode();
        logger.atError().addKeyValue("status", status).log("Ошибка при отправке обновления в Bot");

        if (retryableStatuses.contains(status)) {
            throw new RetryableException("Retryable error from Bot: " + status);
        }

        throw new BotClientException("Ошибка Bot: " + resp.getStatusCode());
    }
}
