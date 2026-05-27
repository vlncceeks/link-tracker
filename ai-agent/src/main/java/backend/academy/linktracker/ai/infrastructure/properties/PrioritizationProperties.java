package backend.academy.linktracker.ai.infrastructure.properties;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.prioritization")
public record PrioritizationProperties(List<String> highKeywords, List<String> lowKeywords) {}
