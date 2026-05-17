package backend.academy.linktracker.bot.application.client.impl;

import backend.academy.linktracker.bot.application.client.ScrapperErrorHandler;
import backend.academy.linktracker.bot.infrastructure.configuration.ScrapperProperties;
import java.net.http.HttpClient;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@Component
public class ScrapperFactory {
    public RestClient createRestClient(ScrapperProperties properties, ObjectMapper objectMapper) {
        Set<HttpStatusCode> retryableStatuses = properties.retryableStatuses().stream()
                .map(HttpStatusCode::valueOf)
                .collect(Collectors.toSet());

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        ClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        ((JdkClientHttpRequestFactory) requestFactory).setReadTimeout(properties.readTimeout());

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .defaultStatusHandler(
                        HttpStatusCode::isError, new ScrapperErrorHandler(objectMapper, retryableStatuses))
                .build();
    }
}
