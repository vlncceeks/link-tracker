package backend.academy.linktracker.scrapper.infrastructure.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.scheduler")
public record SchedulerProperties(long interval, int batchSize) {}
