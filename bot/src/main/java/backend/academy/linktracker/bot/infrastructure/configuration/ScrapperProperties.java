package backend.academy.linktracker.bot.infrastructure.configuration;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scrapper")
public record ScrapperProperties(
        String baseUrl, Duration connectTimeout, Duration readTimeout, List<Integer> retryableStatuses) {}
