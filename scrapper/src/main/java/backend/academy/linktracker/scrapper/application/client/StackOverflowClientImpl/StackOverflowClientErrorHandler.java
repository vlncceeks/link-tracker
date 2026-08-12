package backend.academy.linktracker.scrapper.application.client.StackOverflowClientImpl;

import java.io.IOException;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;

public class StackOverflowClientErrorHandler implements RestClient.ResponseSpec.ErrorHandler {

    @Override
    public void handle(HttpRequest request, ClientHttpResponse response) throws IOException {}
}
