package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import backend.academy.linktracker.scrapper.application.client.MessageSender;
import backend.academy.linktracker.scrapper.application.client.impl.BotClientImpl;
import backend.academy.linktracker.scrapper.application.client.impl.KafkaMessageSender;
import backend.academy.linktracker.scrapper.infrastructure.configuration.BotProperties;
import backend.academy.linktracker.scrapper.infrastructure.configuration.MessageSenderConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class MessageSenderConfigTest {

    @Test
    void whenKafkaType_thenKafkaMessageSenderCreated() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withUserConfiguration(MessageSenderConfig.class)
                .withBean(KafkaTemplate.class, () -> Mockito.mock(KafkaTemplate.class))
                .withBean(BotProperties.class, () -> Mockito.mock(BotProperties.class))
                .withPropertyValues("app.message-sender-type=KAFKA");

        runner.run(context -> {
            assertThat(context.getBean(MessageSender.class)).isInstanceOf(KafkaMessageSender.class);
        });
    }

    @Test
    void whenDirectlyType_thenBotClientCreated() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withUserConfiguration(MessageSenderConfig.class)
                .withBean(KafkaTemplate.class, () -> Mockito.mock(KafkaTemplate.class))
                .withBean(BotProperties.class, () -> Mockito.mock(BotProperties.class))
                .withPropertyValues("app.message-sender-type=DIRECTLY");

        runner.run(context -> {
            assertThat(context.getBean(MessageSender.class)).isInstanceOf(BotClientImpl.class);
        });
    }
}
