package backend.academy.linktracker.bot;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.bot.application.dto.request.LinkUpdateRequest;
import backend.academy.linktracker.bot.infrastructure.kafka.LinkUpdateConsumer;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.google.common.net.HttpHeaders;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@EnableWireMock
@ContextConfiguration(initializers = TestcontainersConfiguration.class)
class ScrapperToBotIntegrationTest {
    @MockitoSpyBean
    private LinkUpdateConsumer linkUpdateConsumer;

    @Autowired
    private KafkaTemplate<String, LinkUpdateRequest> kafkaTemplate;

    @Value("${app.kafka.topic}")
    private String topic;

    @Autowired
    private ApplicationContext context;

    KafkaListenerEndpointRegistry registry;

    @BeforeEach
    void setUp() throws Exception {
        registry = context.getBean(KafkaListenerEndpointRegistry.class);

        stubFor(
                post(urlMatching("/bot[^/]+/sendMessage"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                        .withBody(
                                                "{\"ok\":true,\"result\":{\"message_id\":1,\"chat\":{\"id\":1,\"type\":\"private\"},\"date\":1234567890,\"text\":\"test\"}}")));

        for (MessageListenerContainer container : registry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(container, 1);
        }
    }

    @Test
    void whenScrapperSendsMessage_thenBotConsumerReceivesIt() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        LinkUpdateRequest request =
                new LinkUpdateRequest(1, "https://github.com/user/repo", "Новый коммит", List.of(1L, 2L));

        doAnswer(invocation -> {
                    try {
                        invocation.callRealMethod();
                    } finally {
                        latch.countDown();
                    }
                    return null;
                })
                .when(linkUpdateConsumer)
                .listenUpdate((LinkUpdateRequest) any());

        kafkaTemplate.send(topic, request);

        assertThat(latch.await(30, TimeUnit.SECONDS)).isTrue();
        verify(linkUpdateConsumer, times(1)).listenUpdate((LinkUpdateRequest) any());
    }
}
