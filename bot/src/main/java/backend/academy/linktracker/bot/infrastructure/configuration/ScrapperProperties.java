package backend.academy.linktracker.bot.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scrapper")
public record ScrapperProperties(String baseUrl) {}
