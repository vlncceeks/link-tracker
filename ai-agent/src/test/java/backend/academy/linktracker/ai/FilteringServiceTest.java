package backend.academy.linktracker.ai;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.ai.application.dto.RawUpdate;
import backend.academy.linktracker.ai.infrastructure.properties.FilteringProperties;
import backend.academy.linktracker.ai.infrastructure.service.FilteringService;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FilteringServiceTest {

    private FilteringProperties properties;
    private FilteringService filteringService;

    @BeforeEach
    void setUp() {
        properties = mock(FilteringProperties.class);
        filteringService = new FilteringService(properties);
    }

    @Test
    void shouldReturnFalse_whenDescriptionContainsStopWord() {
        when(properties.minLength()).thenReturn(5);
        when(properties.stopWords()).thenReturn(List.of("spam", "ad"));
        when(properties.excludedAuthors()).thenReturn(List.of());

        RawUpdate update = new RawUpdate(1, "This is a spam message", List.of(123L, 124L));

        assertThat(filteringService.isRelevant(update)).isFalse();
    }

    @Test
    void shouldReturnFalse_whenAuthorIsExcluded() {
        when(properties.minLength()).thenReturn(5);
        when(properties.stopWords()).thenReturn(List.of());
        when(properties.excludedAuthors()).thenReturn(List.of("banned_user"));

        RawUpdate update = new RawUpdate(2, "Normal text by banned_user here", List.of(123L, 124L));

        assertThat(filteringService.isRelevant(update)).isFalse();
    }

    @Test
    void shouldReturnFalse_whenDescriptionIsTooShort() {
        when(properties.minLength()).thenReturn(20);
        when(properties.stopWords()).thenReturn(List.of());
        when(properties.excludedAuthors()).thenReturn(List.of());

        RawUpdate update = new RawUpdate(3, "Short", List.of(123L, 124L));

        assertThat(filteringService.isRelevant(update)).isFalse();
    }

    @Test
    void shouldReturnTrue_whenUpdatePassesAllFilters() {
        when(properties.minLength()).thenReturn(5);
        when(properties.stopWords()).thenReturn(List.of("spam"));
        when(properties.excludedAuthors()).thenReturn(List.of("banned_user"));

        RawUpdate update = new RawUpdate(4, "This is a completely valid update", List.of(123L, 124L));

        assertThat(filteringService.isRelevant(update)).isTrue();
    }
}
