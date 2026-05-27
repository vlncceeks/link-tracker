package backend.academy.linktracker.ai;

import backend.academy.linktracker.ai.application.dto.ProcessedUpdate;
import backend.academy.linktracker.ai.application.dto.RawUpdate;
import backend.academy.linktracker.ai.application.state.Priority;
import backend.academy.linktracker.ai.infrastructure.properties.GroupingProperties;
import backend.academy.linktracker.ai.infrastructure.properties.PrioritizationProperties;
import backend.academy.linktracker.ai.infrastructure.service.GroupingService;
import backend.academy.linktracker.ai.infrastructure.service.PrioritizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GroupingServiceTest {
    private GroupingService service;
    private GroupingProperties groupingProperties;
    private PrioritizationService prioritizationService;
    private PrioritizationProperties prioritizationProperties;

    @BeforeEach
    void setUp() {
        groupingProperties = new GroupingProperties(1000);
        prioritizationProperties = mock(PrioritizationProperties.class);

        prioritizationService = new PrioritizationService(prioritizationProperties);
        service = new GroupingService(prioritizationService, groupingProperties);
    }

    @Test
    void multipleMessagesForOneChat_willBeCombinedIntoOne() throws InterruptedException, ExecutionException {
        RawUpdate update1 = new RawUpdate(1, "author", "Medium priority message", List.of(123L, 124L));
        RawUpdate update2 = new RawUpdate(2, "author", "High priority message", List.of(123L));
        RawUpdate update3 = new RawUpdate(2, "author", "Low priority message", List.of(123L, 124L));

        when(prioritizationProperties.lowKeywords()).thenReturn(List.of("low"));
        when(prioritizationProperties.highKeywords()).thenReturn(List.of("high"));

        Map<Long, CompletableFuture<ProcessedUpdate>> futureMap = service.group(update1);
        service.group(update2);
        service.group(update3);

        ProcessedUpdate result = futureMap.get(123L).get();
        assertThat(result.description())
            .isEqualTo("""
            1. Medium priority message
            2. High priority message
            3. Low priority message
            """);
        assertThat(result.priority()).isEqualTo(Priority.HIGH);

        ProcessedUpdate result2 = futureMap.get(124L).get();
        assertThat(result2.description())
            .isEqualTo("""
            1. Medium priority message
            2. Low priority message
            """);
        assertThat(result2.priority()).isEqualTo(Priority.MEDIUM);
    }

    @Test
    void oneMessageForChat_willBeOne() throws ExecutionException, InterruptedException {
        RawUpdate update = new RawUpdate(1, "author", "Low priority message", List.of(123L, 124L));
        when(prioritizationProperties.lowKeywords()).thenReturn(List.of("low"));
        when(prioritizationProperties.highKeywords()).thenReturn(List.of("high"));

        Map<Long, CompletableFuture<ProcessedUpdate>> futureMap = service.group(update);
        ProcessedUpdate result = futureMap.get(123L).get();

        assertThat(result.description()).isEqualTo("Low priority message");
        assertThat(result.priority()).isEqualTo(Priority.LOW);
    }
}
