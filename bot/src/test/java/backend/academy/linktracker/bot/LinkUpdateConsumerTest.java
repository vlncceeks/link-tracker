package backend.academy.linktracker.bot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.bot.application.dto.request.LinkUpdateRequest;
import backend.academy.linktracker.bot.infrastructure.kafka.LinkUpdateConsumer;
import backend.academy.linktracker.bot.infrastructure.service.UpdateService;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@TestPropertySource(
        properties = {"app.kafka.topic=link-updates-test", "app.kafka.retry.attempts=3", "app.kafka.retry.interval=100"
        })
@ContextConfiguration(initializers = TestcontainersConfiguration.class)
class LinkUpdateConsumerTest {

    @Autowired
    private KafkaTemplate<String, LinkUpdateRequest> kafkaTemplate;

    @MockitoSpyBean
    private LinkUpdateConsumer linkUpdateConsumer;

    @MockitoBean
    private UpdateService updateService;

    @Autowired
    private ConsumerFactory<String, LinkUpdateRequest> consumerFactory;

    private KafkaMessageListenerContainer<String, LinkUpdateRequest> dltContainer;

    private static final String TOPIC = "link-updates-test";
    private static final String DLT_TOPIC = TOPIC + "-dlt";

    private static final LinkUpdateRequest REQUEST =
            new LinkUpdateRequest(1, "https://github.com/user/repo", "Новый коммит", List.of(1L, 2L));

    @BeforeEach
    void setUp() {
        ContainerProperties containerProps = new ContainerProperties(DLT_TOPIC);
        containerProps.setGroupId("test-dlt-group");
        containerProps.setMessageListener((MessageListener<String, LinkUpdateRequest>) record -> {});

        dltContainer = new KafkaMessageListenerContainer<>(consumerFactory, containerProps);
        dltContainer.start();

        ContainerTestUtils.waitForAssignment(dltContainer, 1);
    }

    @AfterEach
    void tearDown() {
        if (dltContainer != null) {
            dltContainer.stop();
        }
    }

    @Test
    void whenConsumerSucceeds_thenNoDltMessage() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        doAnswer(invocation -> {
                    latch.countDown();
                    return null;
                })
                .when(updateService)
                .receive(any());

        kafkaTemplate.send(TOPIC, REQUEST).get();

        assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();

        verify(updateService, times(1)).receive(any());
    }

    @Test
    void whenConsumerFails_thenRetriesAndSendsToDlt() throws Exception {
        CountDownLatch retryLatch = new CountDownLatch(3);
        CountDownLatch dltLatch = new CountDownLatch(1);

        doAnswer(invocation -> {
                    retryLatch.countDown();
                    throw new RuntimeException("fail");
                })
                .when(updateService)
                .receive(any());

        doAnswer(invocation -> {
                    dltLatch.countDown();
                    return null;
                })
                .when(linkUpdateConsumer)
                .handleDltUpdate(any());

        kafkaTemplate.send(TOPIC, REQUEST).get();

        assertThat(retryLatch.await(30, TimeUnit.SECONDS)).isTrue();
        assertThat(dltLatch.await(15, TimeUnit.SECONDS)).isTrue();
        verify(updateService, times(3)).receive(any());
        verify(linkUpdateConsumer, times(1)).handleDltUpdate(any());
    }

    @Test
    void whenDltConsumed_thenOnlyOnce() throws Exception {
        CountDownLatch dltLatch = new CountDownLatch(1);

        doThrow(new RuntimeException("fail")).when(updateService).receive(any());

        doAnswer(invocation -> {
                    dltLatch.countDown();
                    return null;
                })
                .when(linkUpdateConsumer)
                .handleDltUpdate(any());

        kafkaTemplate.send(TOPIC, REQUEST).get();

        assertThat(dltLatch.await(30, TimeUnit.SECONDS)).isTrue();
        Thread.sleep(2000);
        verify(linkUpdateConsumer, times(1)).handleDltUpdate(any());
    }
}
