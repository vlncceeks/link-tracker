package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;

import backend.academy.linktracker.scrapper.application.chat.ChatRepository;
import backend.academy.linktracker.scrapper.application.chat.impl.sql.SqlChatRepository;
import backend.academy.linktracker.scrapper.application.link.LinkRepository;
import backend.academy.linktracker.scrapper.application.link.impl.sql.SqlLinkRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@TestPropertySource(properties = "app.access-type=SQL")
@ContextConfiguration(initializers = TestPostgresConfiguration.class)
public class SqlAccessTypeTest {
    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private LinkRepository linkRepository;

    @Test
    void accessTypeSql_usesSqlImplementations() {
        assertThat(chatRepository).isInstanceOf(SqlChatRepository.class);
        assertThat(linkRepository).isInstanceOf(SqlLinkRepository.class);
    }
}
