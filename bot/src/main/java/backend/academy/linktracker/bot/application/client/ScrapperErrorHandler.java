package backend.academy.linktracker.bot.application.client;

import backend.academy.linktracker.bot.application.dto.response.ApiErrorResponse;
import backend.academy.linktracker.bot.application.exception.ScrapperClientException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
public class ScrapperErrorHandler implements RestClient.ResponseSpec.ErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(ScrapperErrorHandler.class);
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpRequest request, ClientHttpResponse resp) throws IOException {
        try {
            ApiErrorResponse error = objectMapper.readValue(resp.getBody(), ApiErrorResponse.class);
            throw new ScrapperClientException(error.description());
        } catch (ScrapperClientException e) {
            throw e;
        } catch (Exception e) {
            logger.atError()
                    .addKeyValue("status", resp.getStatusCode())
                    .log("Не удалось прочитать тело ошибки от Scrapper");
            throw new ScrapperClientException("Ошибка Scrapper: " + resp.getStatusCode());
        }
    }
}
