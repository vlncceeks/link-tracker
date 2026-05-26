package backend.academy.linktracker.ai;

import backend.academy.linktracker.ai.application.dto.RawUpdate;
import backend.academy.linktracker.ai.application.state.Priority;
import backend.academy.linktracker.ai.infrastructure.properties.PrioritizationProperties;
import backend.academy.linktracker.ai.infrastructure.service.PrioritizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrioritizationServiceTest {
    private PrioritizationService service;
    private PrioritizationProperties properties;

    @BeforeEach
    void setUp() {
        properties = mock(PrioritizationProperties.class);
        service = new PrioritizationService(properties);
    }

    @Test
    void shouldReturnHigh_whenContainsHighKeywords() {
        when(properties.highKeywords()).thenReturn(List.of("critical"));
        when(properties.lowKeywords()).thenReturn(List.of("critical"));

        RawUpdate update = new RawUpdate(1, "author", "critical bug fix", List.of(123L, 124L));
        assertThat(service.prioritize(update)).isEqualTo(Priority.HIGH);
    }

    @Test
    void shouldReturnLow_whenContainsLowKeywords() {
        when(properties.lowKeywords()).thenReturn(List.of("minor"));

        RawUpdate update = new RawUpdate(1, "author", "minor bug fix", List.of(123L, 124L));
        assertThat(service.prioritize(update)).isEqualTo(Priority.LOW);
    }

    @Test
    void shouldReturnMedium_whenNotContainKeywords() {
        when(properties.highKeywords()).thenReturn(List.of("critical"));
        when(properties.lowKeywords()).thenReturn(List.of("minor"));

        RawUpdate update = new RawUpdate(1, "author", "bug fix", List.of(123L, 124L));
        assertThat(service.prioritize(update)).isEqualTo(Priority.MEDIUM);
    }
}
