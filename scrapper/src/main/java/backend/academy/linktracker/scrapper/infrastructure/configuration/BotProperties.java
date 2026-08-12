package backend.academy.linktracker.scrapper.infrastructure.configuration;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bot")
public record BotProperties(String baseUrl, Duration connectTimeout, Duration readTimeout) {}
