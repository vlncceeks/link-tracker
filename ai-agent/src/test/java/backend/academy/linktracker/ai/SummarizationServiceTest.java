package backend.academy.linktracker.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import backend.academy.linktracker.ai.infrastructure.properties.SummarizationProperties;
import backend.academy.linktracker.ai.infrastructure.service.SummarizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SummarizationServiceTest {

    private SummarizationProperties properties;
    private SummarizationService summarizationService;

    @BeforeEach
    void setUp() {
        properties = mock(SummarizationProperties.class);
        summarizationService = new SummarizationService(properties);
    }

    @Test
    void shouldSummarize_whenTextExceedsThreshold() {
        int threshold = 10;
        when(properties.threshold()).thenReturn(threshold);

        String longText = "This is a long description that exceeds the threshold";
        String result = summarizationService.summarize(longText);

        assertThat(result).hasSize(threshold);
        assertThat(result).isEqualTo(longText.substring(0, threshold));
        assertThat(result).isNotEqualTo(longText);
    }

    @Test
    void shouldNotSummarize_whenTextIsBelowThreshold() {
        when(properties.threshold()).thenReturn(100);

        String shortText = "Short text";
        String result = summarizationService.summarize(shortText);

        assertThat(result).isEqualTo(shortText);
    }
}
