package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import backend.academy.linktracker.scrapper.application.client.MessageSender;
import backend.academy.linktracker.scrapper.application.dto.request.LinkUpdateRequest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.wiremock.spring.EnableWireMock;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
@ActiveProfiles("test")
@EnableWireMock
@TestPropertySource(
        properties = {
            "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration",
            "app.access-type=MEMORY"
        })
class FallbackToKafkaTest {

    @MockitoBean(name = "httpMessageSender")
    private MessageSender httpSender;

    @MockitoBean(name = "kafkaMessageSender")
    private MessageSender kafkaSender;

    @Autowired
    private MessageSender messageSender;

    @Test
    void send_shouldFallbackToKafka_whenHttpUnavailable(CapturedOutput output) {
        LinkUpdateRequest request =
                new LinkUpdateRequest(1, "https://github.com/user/repo", "Новый коммит", List.of(1L));

        doThrow(new RuntimeException("HTTP failed")).when(httpSender).send(any());

        messageSender.send(request);

        assertThat(output.getAll()).contains("HTTP sender failed");

        verify(httpSender).send(request);

        verify(kafkaSender).send(request);
    }
}
