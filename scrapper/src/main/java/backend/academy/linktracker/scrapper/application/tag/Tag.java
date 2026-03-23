package backend.academy.linktracker.scrapper.application.tag;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("tags")
public record Tag(@Getter @Id Integer id, @Getter String name) {}
