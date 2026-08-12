package backend.academy.linktracker.scrapper.infrastructure.configuration;

import backend.academy.linktracker.scrapper.application.client.BotClientImpl.BotClientImpl;
import backend.academy.linktracker.scrapper.application.client.BotClientImpl.BotClientWrapper;
import backend.academy.linktracker.scrapper.application.client.KafkaMessageSenderImpl.KafkaMessageSender;
import backend.academy.linktracker.scrapper.application.client.MessageSender;
import backend.academy.linktracker.scrapper.application.client.ResilientMessageSender;
import backend.academy.linktracker.scrapper.application.dto.request.LinkUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@RequiredArgsConstructor
public class MessageSenderConfig {
    @Lazy
    private final KafkaTemplate<String, LinkUpdateRequest> kafkaTemplate;

    @Lazy
    private final BotClientImpl botClient;

    @Bean
    @Qualifier("kafka")
    // @ConditionalOnProperty(name = "app.message-sender-type", havingValue = "KAFKA")
    public MessageSender kafkaMessageSender() {
        return new KafkaMessageSender(kafkaTemplate);
    }

    @Bean
    @Qualifier("http")
    // @ConditionalOnProperty(name = "app.message-sender-type", havingValue = "DIRECTLY")
    public MessageSender httpMessageSender() {
        return new BotClientWrapper(botClient);
    }

    @Bean
    @Primary
    MessageSender messageSender(@Qualifier("http") MessageSender http, @Qualifier("kafka") MessageSender kafka) {
        return new ResilientMessageSender(http, kafka);
    }
}
