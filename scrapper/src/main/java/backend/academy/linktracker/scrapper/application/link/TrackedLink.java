package backend.academy.linktracker.scrapper.application.link;

import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Table("tracked_links")
public class TrackedLink {
    @Id
    private final Integer id;

    @Column("chat_id")
    private final Long chatId;

    private final String url;

    @Setter
    @Column("last_checked_at")
    private Instant lastCheckedAt;

    public TrackedLink(Integer id, Long chatId, String url) {
        this.id = id;
        this.chatId = chatId;
        this.url = url;
        this.lastCheckedAt = Instant.now();
    }
}
