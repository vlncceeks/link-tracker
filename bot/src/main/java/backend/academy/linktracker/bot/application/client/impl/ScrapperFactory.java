package backend.academy.linktracker.bot.application.client.impl;

import backend.academy.linktracker.bot.application.client.ScrapperErrorHandler;
import backend.academy.linktracker.bot.infrastructure.configuration.ScrapperProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@Component
public class ScrapperFactory {
    public RestClient createRestClient(ScrapperProperties properties, ObjectMapper objectMapper) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultStatusHandler(HttpStatusCode::isError, new ScrapperErrorHandler(objectMapper))
                .build();
    }
}
