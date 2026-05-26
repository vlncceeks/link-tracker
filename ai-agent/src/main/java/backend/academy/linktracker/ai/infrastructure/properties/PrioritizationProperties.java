package backend.academy.linktracker.ai.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties(prefix = "app.prioritization")
public record PrioritizationProperties (
    List<String> highKeywords,
    List<String> lowKeywords
) {}
