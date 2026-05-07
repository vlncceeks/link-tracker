package backend.academy.linktracker.scrapper.application.link;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tracked_links")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TrackedLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Long chatId;

    @Column(nullable = false)
    private String url;

    @Setter
    @Column
    private Instant lastCheckedAt;

    public TrackedLink(Integer id, Long chatId, String url) {
        this.id = id;
        this.chatId = chatId;
        this.url = url;
        this.lastCheckedAt = Instant.now();
    }
}
