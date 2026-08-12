package backend.academy.linktracker.scrapper;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.GenericContainer;

@TestConfiguration
public class TestRedisConfiguration implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final GenericContainer<?> VALKEY = new GenericContainer<>("valkey/valkey:8").withExposedPorts(6379);

    static {
        VALKEY.start();
    }

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        TestPropertyValues.of(
                        "spring.data.redis.host=" + VALKEY.getHost(),
                        "spring.data.redis.port=" + VALKEY.getMappedPort(6379),
                        "spring.data.redis.password=")
                .applyTo(context.getEnvironment());
    }
}
