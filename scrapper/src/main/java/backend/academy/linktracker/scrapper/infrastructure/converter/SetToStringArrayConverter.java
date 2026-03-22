package backend.academy.linktracker.scrapper.infrastructure.converter;

import java.util.Set;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class SetToStringArrayConverter implements Converter<Set<String>, String[]> {
    @Override
    public String[] convert(Set<String> source) {
        return source.toArray(new String[0]);
    }
}
