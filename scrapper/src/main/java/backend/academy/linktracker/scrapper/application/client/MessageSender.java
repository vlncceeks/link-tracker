package backend.academy.linktracker.scrapper.application.client;

import backend.academy.linktracker.scrapper.application.dto.request.LinkUpdateRequest;

public interface MessageSender {
    void send(LinkUpdateRequest linkUpdateRequest);
}
