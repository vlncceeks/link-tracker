package backend.academy.linktracker.scrapper.application.client.BotClientImpl;

import backend.academy.linktracker.scrapper.infrastructure.configuration.BotProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class BotClientFactory {
    private static final Logger logger = LoggerFactory.getLogger(BotClientFactory.class);
    private final BotProperties properties;

    public RestClient createRestClient() {
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultStatusHandler(HttpStatusCode::isError, new BotClientErrorHandler())
                .build();
    }
}
