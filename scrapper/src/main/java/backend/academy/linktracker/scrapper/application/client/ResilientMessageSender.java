package backend.academy.linktracker.scrapper.application.client;

import backend.academy.linktracker.scrapper.application.dto.request.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.application.exception.BotClientException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequiredArgsConstructor
public class ResilientMessageSender implements MessageSender {
    private static final Logger logger = LoggerFactory.getLogger(ResilientMessageSender.class);
    private final MessageSender primarySender;
    private final MessageSender fallbackSender;

    @CircuitBreaker(name = "botCB", fallbackMethod = "fallbackSend")
    @Override
    public void send(LinkUpdateRequest linkUpdateRequest) {
        logger.atInfo().log("Sending via HTTP");
        primarySender.send(linkUpdateRequest);
    }

    public void fallbackSend(LinkUpdateRequest linkUpdateRequest, Throwable throwable) {
        logger.atInfo().log("HTTP sender failed. Fallback to Kafka", throwable);
        if (throwable instanceof BotClientException) {
            throw (BotClientException) throwable;
        }
        fallbackSender.send(linkUpdateRequest);
    }
}
