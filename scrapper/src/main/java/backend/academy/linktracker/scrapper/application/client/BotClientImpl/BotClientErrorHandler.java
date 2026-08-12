package backend.academy.linktracker.scrapper.application.client.BotClientImpl;

import backend.academy.linktracker.scrapper.application.exception.BotClientException;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;

public class BotClientErrorHandler implements RestClient.ResponseSpec.ErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(BotClientErrorHandler.class);

    @Override
    public void handle(HttpRequest request, ClientHttpResponse resp) throws IOException {
        logger.atError().addKeyValue("status", resp.getStatusCode()).log("Ошибка при отправке обновления в Bot");
        throw new BotClientException("Ошибка Bot: " + resp.getStatusCode());
    }
}
