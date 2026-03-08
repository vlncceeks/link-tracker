package backend.academy.linktracker.scrapper.properties.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bot")
public record BotProperties(String baseUrl) {}
