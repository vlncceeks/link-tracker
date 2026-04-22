package backend.academy.linktracker.scrapper.application.client.impl;

import backend.academy.linktracker.scrapper.application.client.MessageSender;
import backend.academy.linktracker.scrapper.application.dto.request.LinkUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class KafkaMessageSender implements MessageSender {
    private static final Logger logger = LoggerFactory.getLogger(KafkaMessageSender.class);
    private final KafkaTemplate<String, LinkUpdateRequest> kafkaTemplate;

    @Value("${app.kafka.topic}")
    private String topic;

    @Override
    public void send(LinkUpdateRequest request) {
        CompletableFuture<SendResult<String, LinkUpdateRequest>> future = kafkaTemplate.send(topic, request);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.atInfo().addKeyValue("url", request.url()).setCause(ex).log("Sent message to topic link-updates");
            } else {
                logger.atError().addKeyValue("url", request.url()).setCause(ex).log("Unable to sent message to topic link-updates");
            }
        });
    }
}
