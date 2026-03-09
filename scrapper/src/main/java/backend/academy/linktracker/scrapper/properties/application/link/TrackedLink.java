package backend.academy.linktracker.scrapper.properties.application.link;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
public class TrackedLink {
    private final Integer id;
    private final String url;
    private final Set<String> tags;
    private final Set<String> filters;

    @Setter
    private Instant lastCheckedAt;

    public TrackedLink(Integer id, String url, List<String> tags, List<String> filters) {
        this.id = id;
        this.url = url;
        this.tags = new HashSet<>(tags != null ? tags : List.of());
        this.filters = new HashSet<>(filters != null ? filters : List.of());
        this.lastCheckedAt = Instant.now();
    }
}
