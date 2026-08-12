package backend.academy.linktracker.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.ai.application.dto.ProcessedUpdate;
import backend.academy.linktracker.ai.application.dto.RawUpdate;
import backend.academy.linktracker.ai.application.sender.KafkaMessageSender;
import backend.academy.linktracker.ai.application.state.Priority;
import backend.academy.linktracker.ai.infrastructure.service.FilteringService;
import backend.academy.linktracker.ai.infrastructure.service.GroupingService;
import backend.academy.linktracker.ai.infrastructure.service.ProcessUpdateService;
import backend.academy.linktracker.ai.infrastructure.service.SummarizationService;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ContextConfiguration(initializers = TestcontainersConfiguration.class)
@TestPropertySource(properties = "app.kafka.input-topic=" + KafkaProducerIntegrationTest.TOPIC)
class KafkaProducerIntegrationTest {
    static final String TOPIC = "processed-updates";

    private FilteringService filteringService;
    private SummarizationService summarizationService;
    private GroupingService groupingService;
    private ProcessUpdateService processUpdateService;

    @Autowired
    private KafkaMessageSender kafkaMessageSender;

    private Consumer<String, ProcessedUpdate> consumer;

    @BeforeEach
    void setUp() {
        consumer = createKafkaConsumer("test-group");
        consumer.subscribe(List.of(TOPIC));

        filteringService = mock(FilteringService.class);
        summarizationService = mock(SummarizationService.class);
        groupingService = mock(GroupingService.class);

        processUpdateService =
                new ProcessUpdateService(filteringService, summarizationService, groupingService, kafkaMessageSender);
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    private KafkaConsumer<String, ProcessedUpdate> createKafkaConsumer(String groupId) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, TestcontainersConfiguration.KAFKA.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, ProcessedUpdate.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new KafkaConsumer<>(props);
    }

    @Test
    void messagePassAllSteps_shouldBePublishedToKafka() {
        RawUpdate update = new RawUpdate(1, "author", "Normal text here", List.of(123L, 124L));
        ProcessedUpdate processedUpdate = new ProcessedUpdate(1, "Normal text here", List.of(123L), Priority.MEDIUM);
        Map<Long, CompletableFuture<ProcessedUpdate>> futures =
                Map.ofEntries(Map.entry(123L, CompletableFuture.completedFuture(processedUpdate)));

        when(filteringService.isRelevant(update)).thenReturn(true);
        when(summarizationService.summarize(update.description())).thenReturn("Normal text here");
        when(groupingService.group(update)).thenReturn(futures);

        processUpdateService.processUpdate(update);

        ConsumerRecords<String, ProcessedUpdate> records = consumer.poll(Duration.ofSeconds(5));

        assertThat(records.count()).isEqualTo(1);

        ProcessedUpdate value = records.iterator().next().value();

        assertThat(value.description()).isEqualTo("Normal text here");
    }

    @Test
    void messageHasBeenFiltrated_shouldNotBePublishedToKafka() {
        RawUpdate update = new RawUpdate(1, "author", "Normal text here", List.of(123L, 124L));
        when(filteringService.isRelevant(update)).thenReturn(false);

        processUpdateService.processUpdate(update);

        ConsumerRecords<String, ProcessedUpdate> records = consumer.poll(Duration.ofSeconds(5));

        assertThat(records.count()).isEqualTo(0);
    }
}
