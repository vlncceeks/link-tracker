package backend.academy.linktracker.scrapper.application.client.BotClientImpl;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Getter
@Component
public class BotClientImpl{
    private final RestClient restClient;

    public BotClientImpl(BotClientFactory clientFactory) {
        this.restClient = clientFactory.createRestClient();
    }
}
