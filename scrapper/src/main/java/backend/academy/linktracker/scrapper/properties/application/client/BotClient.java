package backend.academy.linktracker.scrapper.properties.application.client;

import backend.academy.linktracker.scrapper.properties.application.dto.request.LinkUpdateRequest;

public interface BotClient {
    void sendUpdate(LinkUpdateRequest request);
}
