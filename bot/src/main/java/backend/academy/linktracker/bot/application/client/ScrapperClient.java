package backend.academy.linktracker.bot.application.client;

import backend.academy.linktracker.bot.application.dto.request.AddLinkRequest;
import backend.academy.linktracker.bot.application.dto.request.RemoveLinkRequest;
import backend.academy.linktracker.bot.application.dto.response.LinkResponse;
import backend.academy.linktracker.bot.application.dto.response.ListLinksResponse;

public interface ScrapperClient {
    void registerChat(Long chatId);
    void deleteChat(Long chatId);
    ListLinksResponse getLinks(Long chatId);
    LinkResponse addLink(Long chatId, AddLinkRequest request);
    LinkResponse removeLink(Long chatId, RemoveLinkRequest request);
}
