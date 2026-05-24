package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import backend.academy.linktracker.scrapper.application.client.MessageSender;
import backend.academy.linktracker.scrapper.application.dto.request.LinkUpdateRequest;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = {"app.kafka.topic=link-updates-test", "app.scheduler.ai-enabled=false"})
@ContextConfiguration(initializers = {TestPostgresConfiguration.class, TestKafkaConfiguration.class})
class KafkaMessageSenderTest {

    @Autowired
    @Qualifier("kafka")
    private MessageSender kafkaMessageSender;

    private Consumer<String, LinkUpdateRequest> consumer;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @BeforeEach
    void setUp() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        consumerProps.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, LinkUpdateRequest.class);

        consumer = new DefaultKafkaConsumerFactory<String, LinkUpdateRequest>(consumerProps).createConsumer();
        consumer.subscribe(List.of("link-updates-test"));
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    void send_messageGoesToCorrectTopicWithCorrectContent() {
        LinkUpdateRequest request =
                new LinkUpdateRequest(1, "https://github.com/user/repo", "Новый коммит", List.of(1L, 2L));

        kafkaMessageSender.send(request);

        ConsumerRecords<String, LinkUpdateRequest> records = ConsumerRecords.empty();
        long deadline = System.currentTimeMillis() + 5000;
        while (records.isEmpty() && System.currentTimeMillis() < deadline) {
            records = consumer.poll(Duration.ofMillis(500));
        }

        assertThat(records.count()).isEqualTo(1);

        ConsumerRecord<String, LinkUpdateRequest> record = records.iterator().next();

        assertThat(record.topic()).isEqualTo("link-updates-test");

        LinkUpdateRequest received = record.value();
        assertThat(received.id()).isEqualTo(request.id());
        assertThat(received.url()).isEqualTo(request.url());
        assertThat(received.description()).isEqualTo(request.description());
        assertThat(received.chatIds()).isEqualTo(request.chatIds());
    }
}
