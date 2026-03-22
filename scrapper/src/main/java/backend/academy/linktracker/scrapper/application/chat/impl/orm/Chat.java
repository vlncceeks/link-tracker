package backend.academy.linktracker.scrapper.application.chat.impl.orm;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("chats")
public record Chat (@Id Long id) {}
