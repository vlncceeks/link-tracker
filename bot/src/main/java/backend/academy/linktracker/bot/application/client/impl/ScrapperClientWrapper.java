package backend.academy.linktracker.bot.application.client.impl;

import backend.academy.linktracker.bot.application.client.ScrapperClient;
import backend.academy.linktracker.bot.application.dto.request.AddLinkRequest;
import backend.academy.linktracker.bot.application.dto.request.RemoveLinkRequest;
import backend.academy.linktracker.bot.application.dto.response.LinkResponse;
import backend.academy.linktracker.bot.application.dto.response.ListLinksResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScrapperClientWrapper implements ScrapperClient {
    private static final String TG_CHAT_ID = "Tg-Chat-Id";
    private static final Logger logger = LoggerFactory.getLogger(ScrapperClientWrapper.class);

    private final ScrapperClientImpl client;

    @CircuitBreaker(name = "scrapperCB")
    @Retry(name = "scrapperRetry")
    @Override
    public void registerChat(Long chatId) {
        logger.atInfo().addKeyValue("chatId", chatId).log("Регистрация чата в Scrapper");
        client.getRestClient().post().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();
    }

    @CircuitBreaker(name = "scrapperCB")
    @Retry(name = "scrapperRetry")
    @Override
    public void deleteChat(Long chatId) {
        logger.atInfo().addKeyValue("chatId", chatId).log("Удаление чата из Scrapper");
        client.getRestClient().delete().uri("/tg-chat/{id}", chatId).retrieve().toBodilessEntity();
    }

    @CircuitBreaker(name = "scrapperCB")
    @Retry(name = "scrapperRetry")
    @Override
    public ListLinksResponse getLinks(Long chatId) {
        return client.getRestClient()
                .get()
                .uri("/links")
                .header(TG_CHAT_ID, chatId.toString())
                .retrieve()
                .body(ListLinksResponse.class);
    }

    @CircuitBreaker(name = "scrapperCB")
    @Retry(name = "scrapperRetry")
    @Override
    public LinkResponse addLink(Long chatId, AddLinkRequest request) {
        logger.atInfo()
                .addKeyValue("chatId", chatId)
                .addKeyValue("url", request.url())
                .log("Добавление ссылки через Scrapper");
        return client.getRestClient()
                .post()
                .uri("/links")
                .header(TG_CHAT_ID, chatId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(LinkResponse.class);
    }

    @CircuitBreaker(name = "scrapperCB")
    @Retry(name = "scrapperRetry")
    @Override
    public LinkResponse removeLink(Long chatId, RemoveLinkRequest request) {
        logger.atInfo()
                .addKeyValue("chatId", chatId)
                .addKeyValue("url", request.url())
                .log("Удаление ссылки через Scrapper");
        return client.getRestClient()
                .method(HttpMethod.DELETE)
                .uri("/links")
                .header(TG_CHAT_ID, chatId.toString())
                .body(request)
                .retrieve()
                .body(LinkResponse.class);
    }
}
