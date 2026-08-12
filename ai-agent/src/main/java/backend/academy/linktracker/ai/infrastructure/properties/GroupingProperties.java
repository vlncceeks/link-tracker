package backend.academy.linktracker.ai.infrastructure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.grouping")
public record GroupingProperties(Integer windowMs) {}
