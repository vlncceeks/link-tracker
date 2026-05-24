package backend.academy.linktracker.scrapper.application.client.KafkaMessageSenderImpl;

import backend.academy.linktracker.scrapper.application.client.MessageSender;
import backend.academy.linktracker.scrapper.application.dto.request.LinkUpdateRequest;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

@RequiredArgsConstructor
public class KafkaMessageSender implements MessageSender {
    private static final Logger logger = LoggerFactory.getLogger(KafkaMessageSender.class);
    private final KafkaTemplate<String, LinkUpdateRequest> kafkaTemplate;

    @Value("${app.kafka.topic}")
    private String topic;

    @Value("${app.kafka.ai-topic}")
    private String aiTopic;

    @Value("${app.scheduler.ai-enabled}")
    private boolean schedulerAiEnabled;

    @Override
    public void send(LinkUpdateRequest request) {
        CompletableFuture<SendResult<String, LinkUpdateRequest>> future;
        if (schedulerAiEnabled) future = kafkaTemplate.send(aiTopic, request);
        else future = kafkaTemplate.send(topic, request);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.atInfo()
                        .addKeyValue("url", request.url())
                        .log("Sent message to topic " + (schedulerAiEnabled ? aiTopic : topic));
            } else {
                logger.atError()
                        .addKeyValue("url", request.url())
                        .setCause(ex)
                        .log("Unable to sent message to topic " + (schedulerAiEnabled ? aiTopic : topic));
            }
        });
    }
}
