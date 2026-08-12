package backend.academy.linktracker.ai.infrastructure.properties;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.filtering")
public record FilteringProperties(List<String> stopWords, Integer minLength, List<String> excludedAuthors) {}
