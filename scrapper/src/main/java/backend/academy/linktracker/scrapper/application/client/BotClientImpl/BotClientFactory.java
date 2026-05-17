package backend.academy.linktracker.scrapper.application.client.BotClientImpl;

import backend.academy.linktracker.scrapper.infrastructure.configuration.BotProperties;
import java.net.http.HttpClient;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class BotClientFactory {
    private final BotProperties properties;

    public RestClient createRestClient() {
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
                .defaultStatusHandler(HttpStatusCode::isError, new BotClientErrorHandler(retryableStatuses))
                .build();
    }
}
