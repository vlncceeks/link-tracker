package backend.academy.linktracker.ai.infrastructure.kafka;

import backend.academy.linktracker.ai.application.dto.RawUpdate;
import backend.academy.linktracker.ai.infrastructure.service.ProcessUpdateService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaConsumer {
    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumer.class);
    private final ProcessUpdateService processUpdateService;

    @KafkaListener(topics = "${app.kafka.input-topic}", groupId = "ai-group")
    public void consume(RawUpdate rawUpdate) {
        logger.atInfo()
                .addKeyValue("id", rawUpdate.id())
                .addKeyValue(
                        "chatCount",
                        rawUpdate.chatIds() != null ? rawUpdate.chatIds().size() : 0)
                .log("Получено обновление ссылки");

        processUpdateService.processUpdate(rawUpdate);
    }
}
