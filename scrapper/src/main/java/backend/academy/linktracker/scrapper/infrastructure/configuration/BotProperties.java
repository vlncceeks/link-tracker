package backend.academy.linktracker.scrapper.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "bot")
public record BotProperties(
    String baseUrl,
    Duration connectTimeout,
    Duration readTimeout,
    List<Integer> retryableStatuses
) {}
