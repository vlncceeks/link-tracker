package backend.academy.linktracker.scrapper.properties.application.client.impl;

import backend.academy.linktracker.scrapper.properties.application.client.BotClient;
import backend.academy.linktracker.scrapper.properties.application.dto.request.LinkUpdateRequest;
import backend.academy.linktracker.scrapper.properties.application.exception.BotClientException;
import backend.academy.linktracker.scrapper.properties.infrastructure.configuration.BotProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class BotClientImpl implements BotClient {
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
    public void sendUpdate(LinkUpdateRequest request) {
        restClient.post().uri("/updates").body(request).retrieve().toBodilessEntity();
    }
}
