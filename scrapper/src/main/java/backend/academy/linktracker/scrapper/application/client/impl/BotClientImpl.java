package backend.academy.linktracker.scrapper.application.client.impl;

import backend.academy.linktracker.scrapper.application.client.MessageSender;
import backend.academy.linktracker.scrapper.application.dto.request.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.application.exception.BotClientException;
import backend.academy.linktracker.scrapper.infrastructure.configuration.BotProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

public class BotClientImpl implements MessageSender {
    private static final Logger logger = LoggerFactory.getLogger(BotClientImpl.class);

    private final RestClient restClient;

    public BotClientImpl(BotProperties properties) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultStatusHandler(HttpStatusCode::isError, (req, resp) -> {
                    logger.atError()
                            .addKeyValue("status", resp.getStatusCode())
                            .log("Ошибка при отправке обновления в Bot");
                    throw new BotClientException("Ошибка Bot: " + resp.getStatusCode());
                })
                .build();
    }

    @Override
    public void send(LinkUpdateRequest request) {
        try {
            restClient.post().uri("/updates").body(request).retrieve().toBodilessEntity();
        } catch (ResourceAccessException e) {
            logger.atError().addKeyValue("url", request.url()).setCause(e).log("Unable to connect to the bot client");
            throw new BotClientException("Bot client is unavailable: " + e.getMessage());
        }
    }
}
