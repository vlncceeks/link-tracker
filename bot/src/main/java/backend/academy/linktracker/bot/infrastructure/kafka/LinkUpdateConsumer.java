package backend.academy.linktracker.bot.infrastructure.kafka;

import backend.academy.linktracker.bot.application.dto.request.LinkUpdateRequest;
import backend.academy.linktracker.bot.infrastructure.service.UpdateService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LinkUpdateConsumer {
    private static final Logger logger = LoggerFactory.getLogger(LinkUpdateConsumer.class);
    private final UpdateService updateService;

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "bot-group")
    public void listenUpdate(LinkUpdateRequest request) {
        logger.atInfo()
            .addKeyValue("url", request.url())
            .addKeyValue("chatCount", request.chatIds() != null ? request.chatIds().size() : 0)
            .log("Получено обновление ссылки");

        updateService.receive(request);
    }
}
