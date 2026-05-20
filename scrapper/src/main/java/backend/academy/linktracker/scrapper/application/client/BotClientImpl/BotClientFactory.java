package backend.academy.linktracker.scrapper.application.client.BotClientImpl;

import backend.academy.linktracker.scrapper.infrastructure.configuration.BotProperties;
import java.net.http.HttpClient;
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
