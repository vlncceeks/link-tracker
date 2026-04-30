package backend.academy.linktracker.scrapper.application.client.StackOverflowClientImpl;

import backend.academy.linktracker.scrapper.application.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.application.dto.response.StackOverflowAnswerResponse;
import backend.academy.linktracker.scrapper.application.dto.response.StackOverflowCommentResponse;
import backend.academy.linktracker.scrapper.application.dto.response.StackOverflowResponse;
import backend.academy.linktracker.scrapper.infrastructure.configuration.StackoverflowProperties;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@Getter
public class StackOverflowClientImpl{
    private final RestClient restClient;

    public StackOverflowClientImpl(StackOverflowClientFactory factory) {
        this.restClient = factory.createRestClient();
    }
}
