package backend.academy.linktracker.scrapper.application.linktag;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("link_tags")
public record LinkTag(
        @Getter @Id Integer id,
        @Getter @Column("link_id") Integer linkId,
        @Getter @Column("tag_id") Integer tagId) {}
