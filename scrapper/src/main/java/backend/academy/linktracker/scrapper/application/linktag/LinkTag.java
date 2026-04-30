package backend.academy.linktracker.scrapper.application.linktag;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "link_tags")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LinkTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "link_id")
    private Integer linkId;

    @Column(name = "tag_id")
    private Integer tagId;

    public LinkTag(Integer linkId, Integer tagId) {
        this.linkId = linkId;
        this.tagId = tagId;
    }
}
