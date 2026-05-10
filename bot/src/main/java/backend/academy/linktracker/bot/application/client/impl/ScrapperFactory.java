package backend.academy.linktracker.bot.application.client.impl;

import backend.academy.linktracker.bot.application.client.ScrapperErrorHandler;
import backend.academy.linktracker.bot.infrastructure.configuration.ScrapperProperties;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;
import java.net.http.HttpClient;

@Component
public class ScrapperFactory {
    public RestClient createRestClient(ScrapperProperties properties, ObjectMapper objectMapper) {
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(properties.connectTimeout())
            .build();

        ClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(client);

        ((JdkClientHttpRequestFactory) factory).setReadTimeout(properties.readTimeout());

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(factory)
                .defaultStatusHandler(HttpStatusCode::isError, new ScrapperErrorHandler(objectMapper))
                .build();
    }
}
