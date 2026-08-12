package backend.academy.linktracker.bot.application.client.impl;

import backend.academy.linktracker.bot.infrastructure.configuration.ScrapperProperties;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@Component
@Getter
public class ScrapperClientImpl {
    private final RestClient restClient;

    public ScrapperClientImpl(ScrapperFactory factory, ScrapperProperties properties, ObjectMapper objectMapper) {
        this.restClient = factory.createRestClient(properties, objectMapper);
    }
}
