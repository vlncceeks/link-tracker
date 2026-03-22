package backend.academy.linktracker.scrapper.application.link;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("tracked_links ")
public class TrackedLink {
    @Id
    private final Integer id;
    @Column("chat_id")
    private final Long chatId;
    private final String url;
    private final Set<String> tags;
    private final Set<String> filters;

    @Setter
    private Instant lastCheckedAt;

    public TrackedLink(Integer id, Long chatId, String url, Set<String> tags, Set<String> filters) {
        this.id = id;
        this.chatId = chatId;
        this.url = url;
        this.tags = tags != null ? tags : Set.of();
        this.filters = filters != null ? filters : Set.of();
        this.lastCheckedAt = Instant.now();
    }
}
