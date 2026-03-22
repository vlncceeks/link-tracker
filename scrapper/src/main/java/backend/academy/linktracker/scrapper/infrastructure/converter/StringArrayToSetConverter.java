package backend.academy.linktracker.scrapper.infrastructure.converter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class StringArrayToSetConverter implements Converter<String[], Set<String>> {
    @Override
    public Set<String> convert(String[] source) {
        return new HashSet<>(Arrays.asList(source));
    }
}
