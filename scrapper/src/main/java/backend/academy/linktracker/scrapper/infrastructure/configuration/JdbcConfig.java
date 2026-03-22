package backend.academy.linktracker.scrapper.infrastructure.configuration;

import backend.academy.linktracker.scrapper.infrastructure.converter.SetToStringArrayConverter;
import backend.academy.linktracker.scrapper.infrastructure.converter.StringArrayToSetConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import java.util.List;

@Configuration
public class JdbcConfig extends AbstractJdbcConfiguration {
    @Override
    protected List<?> userConverters() {
        return List.of(
            new SetToStringArrayConverter(),
            new StringArrayToSetConverter()
        );
    }
}
