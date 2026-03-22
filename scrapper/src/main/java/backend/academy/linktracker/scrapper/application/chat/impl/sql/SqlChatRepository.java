package backend.academy.linktracker.scrapper.application.chat.impl.sql;

import backend.academy.linktracker.scrapper.application.chat.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;


@RequiredArgsConstructor
public class SqlChatRepository implements ChatRepository {
    private final JdbcClient jdbcClient;

    @Override
    public void register(Long chatId) {
        jdbcClient.sql("INSERT INTO chats (id) VALUES (?)")
            .param(chatId)
            .update();
    }

    @Override
    public void delete(Long chatId) {
        jdbcClient.sql("DELETE FROM chats WHERE id = ?")
            .param(chatId)
            .update();
    }

    @Override
    public boolean exists(Long chatId) {
        return jdbcClient.sql("SELECT COUNT(*) FROM chats WHERE id = ?")
            .param(chatId)
            .query(Integer.class)
            .single() > 0;
    }
}
