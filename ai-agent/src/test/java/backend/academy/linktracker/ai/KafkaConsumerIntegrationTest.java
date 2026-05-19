package backend.academy.linktracker.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.ai.application.dto.RawUpdate;
import backend.academy.linktracker.ai.infrastructure.service.ProcessUpdateService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
@ContextConfiguration(initializers = TestcontainersConfiguration.class)
@TestPropertySource(properties = "app.kafka.input-topic=" + KafkaConsumerIntegrationTest.TOPIC)
class KafkaConsumerIntegrationTest {

    static final String TOPIC = "raw-updates";

    @MockitoSpyBean
    private ProcessUpdateService processUpdateService;

    @Autowired
    private KafkaTemplate<String, RawUpdate> kafkaTemplate;

    @Test
    void shouldConsumeValidMessage_andPassToProcessUpdateService() throws Exception {
        RawUpdate rawUpdate = new RawUpdate(1, "Valid description", List.of(100L, 200L));

        kafkaTemplate.send(TOPIC, rawUpdate);

        ArgumentCaptor<RawUpdate> captor = ArgumentCaptor.forClass(RawUpdate.class);
        verify(processUpdateService, timeout(10_000).times(1)).processUpdate(captor.capture());

        RawUpdate captured = captor.getValue();
        assertThat(captured.id()).isEqualTo(1);
        assertThat(captured.description()).isEqualTo("Valid description");
        assertThat(captured.chatIds()).containsExactly(100L, 200L);
    }

    @Test
    void shouldNotCrash_whenMessageHasInvalidFormat() throws Exception {
        try (KafkaProducer<String, String> rawProducer = createRawStringProducer()) {
            rawProducer.send(new ProducerRecord<>(TOPIC, "invalid-json-{broken"));
            rawProducer.flush();
        }

        Thread.sleep(3000);

        verify(processUpdateService, never()).processUpdate(any());
    }

    private KafkaProducer<String, String> createRawStringProducer() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, TestcontainersConfiguration.KAFKA.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        return new KafkaProducer<>(props);
    }
}
