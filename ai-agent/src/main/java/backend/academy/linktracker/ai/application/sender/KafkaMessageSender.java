package backend.academy.linktracker.ai.application.sender;

import backend.academy.linktracker.ai.application.dto.ProcessedUpdate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class KafkaMessageSender implements MessageSender {
    private static final Logger logger = LoggerFactory.getLogger(KafkaMessageSender.class);
    private final KafkaTemplate<String, ProcessedUpdate> kafkaTemplate;

    @Value("${app.kafka.output-topic}")
    private String topic;

    @Override
    public void send(ProcessedUpdate processedUpdate) {
        CompletableFuture<SendResult<String, ProcessedUpdate>> future = kafkaTemplate.send(topic, processedUpdate);

        future.whenComplete((stringProcessedUpdateSendResult,throwable) -> {
            if (throwable != null) {
                logger.atError()
                    .addKeyValue("id", processedUpdate.id())
                    .setCause(throwable)
                    .log("Unable to sent message to topic " + topic);
            }
            else {
                logger.atInfo()
                    .addKeyValue("id", processedUpdate.id())
                    .log("Sent message to topic " + topic);
            }
        });
    }
}
