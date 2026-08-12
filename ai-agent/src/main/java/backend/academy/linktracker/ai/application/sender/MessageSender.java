package backend.academy.linktracker.ai.application.sender;

import backend.academy.linktracker.ai.application.dto.ProcessedUpdate;

public interface MessageSender {
    void send(ProcessedUpdate processedUpdate);
}
