package backend.academy.linktracker.scrapper.properties.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "scheduler")
public record SchedulerProperties(long interval) {}
