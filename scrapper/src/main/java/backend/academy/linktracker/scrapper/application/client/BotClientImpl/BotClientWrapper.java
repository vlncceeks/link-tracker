package backend.academy.linktracker.scrapper.application.client.BotClientImpl;

import backend.academy.linktracker.scrapper.application.client.MessageSender;
import backend.academy.linktracker.scrapper.application.dto.request.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.application.exception.BotClientException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;

@RequiredArgsConstructor
public class BotClientWrapper implements MessageSender {
    private static final Logger logger = LoggerFactory.getLogger(BotClientWrapper.class);
    private final BotClientImpl botClient;

    @CircuitBreaker(name = "botCB")
    @Retry(name = "botRetry")
    @Override
    public void send(LinkUpdateRequest request) {
        try {
            botClient
                    .getRestClient()
                    .post()
                    .uri("/updates")
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (ResourceAccessException e) {
            logger.atError().addKeyValue("url", request.url()).setCause(e).log("Unable to connect to the bot client");
            throw new BotClientException("Bot client is unavailable: " + e.getMessage());
        }
    }
}
