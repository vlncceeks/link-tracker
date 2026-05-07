package backend.academy.linktracker.scrapper.infrastructure.configuration;

import backend.academy.linktracker.scrapper.application.client.BotClientImpl.BotClientImpl;
import backend.academy.linktracker.scrapper.application.client.BotClientImpl.BotClientWrapper;
import backend.academy.linktracker.scrapper.application.client.KafkaMessageSenderImpl.KafkaMessageSender;
import backend.academy.linktracker.scrapper.application.client.MessageSender;
import backend.academy.linktracker.scrapper.application.dto.request.LinkUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@RequiredArgsConstructor
public class MessageSenderConfig {
    @Lazy
    private final KafkaTemplate<String, LinkUpdateRequest> kafkaTemplate;

    @Lazy
    private final BotClientImpl botClient;

    @Bean
    @ConditionalOnProperty(name = "app.message-sender-type", havingValue = "KAFKA")
    public MessageSender kafkaMessageSender() {
        return new KafkaMessageSender(kafkaTemplate);
    }

    @Bean
    @ConditionalOnProperty(name = "app.message-sender-type", havingValue = "DIRECTLY")
    public MessageSender messageSender() {
        return new BotClientWrapper(botClient);
    }
}
