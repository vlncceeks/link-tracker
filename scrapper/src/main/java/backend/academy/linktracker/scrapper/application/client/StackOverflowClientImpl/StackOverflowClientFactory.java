package backend.academy.linktracker.scrapper.application.client.StackOverflowClientImpl;

import backend.academy.linktracker.scrapper.infrastructure.configuration.StackoverflowProperties;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class StackOverflowClientFactory {
    private static final Logger logger = LoggerFactory.getLogger(StackOverflowClientFactory.class);
    private final StackoverflowProperties properties;

    public RestClient createRestClient() {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestInterceptor((request, body, execution) -> {
                    URI withParams = UriComponentsBuilder.fromUri(request.getURI())
                            .queryParam("site", "stackoverflow")
                            .queryParam("key", properties.getKey())
                            .build()
                            .toUri();
                    return execution.execute(
                            new HttpRequestWrapper(request) {
                                @Override
                                public URI getURI() {
                                    return withParams;
                                }
                            },
                            body);
                })
                .build();
    }
}
