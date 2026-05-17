package backend.academy.linktracker.scrapper.infrastructure.configuration;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bot")
public record BotProperties(
        String baseUrl, Duration connectTimeout, Duration readTimeout, List<Integer> retryableStatuses) {}
