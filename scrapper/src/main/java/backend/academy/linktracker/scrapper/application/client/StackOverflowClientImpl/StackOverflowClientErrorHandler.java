package backend.academy.linktracker.scrapper.application.client.StackOverflowClientImpl;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;
import java.io.IOException;

public class StackOverflowClientErrorHandler implements RestClient.ResponseSpec.ErrorHandler {

    @Override
    public void handle(HttpRequest request, ClientHttpResponse response) throws IOException {

    }
}
