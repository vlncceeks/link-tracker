package backend.academy.linktracker.bot.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.Duration;
import java.util.List;

@ConfigurationProperties(prefix = "scrapper")
public record ScrapperProperties(
    String baseUrl,
    Duration connectTimeout,
    Duration readTimeout,
    List<Integer> retryableStatuses
) {}
