package backend.academy.linktracker.bot.application.client.impl;

import backend.academy.linktracker.bot.application.client.ScrapperClient;
import backend.academy.linktracker.bot.application.dto.request.AddLinkRequest;
import backend.academy.linktracker.bot.application.dto.request.RemoveLinkRequest;
import backend.academy.linktracker.bot.application.dto.response.ApiErrorResponse;
import backend.academy.linktracker.bot.application.dto.response.LinkResponse;
import backend.academy.linktracker.bot.application.dto.response.ListLinksResponse;
import backend.academy.linktracker.bot.application.exception.ScrapperClientException;
import backend.academy.linktracker.bot.infrastructure.configuration.ScrapperProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@Component
public class ScrapperClientImpl implements ScrapperClient {
    private static final Logger logger = LoggerFactory.getLogger(ScrapperClientImpl.class);
    private static final String TG_CHAT_ID = "Tg-Chat-Id";

    private final RestClient restClient;

    public ScrapperClientImpl(ScrapperProperties properties, ObjectMapper objectMapper) {
        this.restClient = RestClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultStatusHandler(HttpStatusCode::isError, (req, resp) -> {
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
                })
                .build();
    }

    @Override
    public void registerChat(Long chatId) {
        logger.atInfo().addKeyValue("chatId", chatId).log("Регистрация чата в Scrapper");
        restClient.post().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();
    }

    @Override
    public void deleteChat(Long chatId) {
        logger.atInfo().addKeyValue("chatId", chatId).log("Удаление чата из Scrapper");
        restClient.delete().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();
    }

    @Override
    public ListLinksResponse getLinks(Long chatId) {
        return restClient
                .get()
                .uri("/links")
                .header(TG_CHAT_ID, chatId.toString())
                .retrieve()
                .body(ListLinksResponse.class);
    }

    @Override
    public LinkResponse addLink(Long chatId, AddLinkRequest request) {
        logger.atInfo()
                .addKeyValue("chatId", chatId)
                .addKeyValue("url", request.url())
                .log("Добавление ссылки через Scrapper");
        return restClient
                .post()
                .uri("/links")
                .header(TG_CHAT_ID, chatId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(LinkResponse.class);
    }

    @Override
    public LinkResponse removeLink(Long chatId, RemoveLinkRequest request) {
        logger.atInfo()
                .addKeyValue("chatId", chatId)
                .addKeyValue("url", request.url())
                .log("Удаление ссылки через Scrapper");
        return restClient
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header(TG_CHAT_ID, chatId.toString())
                .body(request)
                .retrieve()
                .body(LinkResponse.class);
    }
}
