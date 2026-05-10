package backend.academy.linktracker.scrapper.application.client.BotClientImpl;

import backend.academy.linktracker.scrapper.infrastructure.configuration.BotProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.net.http.HttpClient;

@Component
@RequiredArgsConstructor
public class BotClientFactory {
    private final BotProperties properties;

    public RestClient createRestClient() {
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(properties.connectTimeout())
            .version(HttpClient.Version.HTTP_1_1)
            .build();

        ClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(client);

        ((JdkClientHttpRequestFactory) factory).setReadTimeout(properties.readTimeout());

        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(factory)
                .defaultStatusHandler(HttpStatusCode::isError, new BotClientErrorHandler())
                .build();
    }
}
