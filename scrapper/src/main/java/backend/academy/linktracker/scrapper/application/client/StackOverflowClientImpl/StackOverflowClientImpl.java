package backend.academy.linktracker.scrapper.application.client.StackOverflowClientImpl;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@Getter
public class StackOverflowClientImpl {
    private final RestClient restClient;

    public StackOverflowClientImpl(StackOverflowClientFactory factory) {
        this.restClient = factory.createRestClient();
    }
}
