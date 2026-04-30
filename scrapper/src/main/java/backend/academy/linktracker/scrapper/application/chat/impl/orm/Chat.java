package backend.academy.linktracker.scrapper.application.chat.impl.orm;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chats")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Chat {
    @Id
    private Long id;
}
