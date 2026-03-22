package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import backend.academy.linktracker.scrapper.application.chat.ChatRepository;
import backend.academy.linktracker.scrapper.application.chat.impl.sql.SqlChatRepository;
import backend.academy.linktracker.scrapper.application.link.LinkRepository;
import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import backend.academy.linktracker.scrapper.application.link.impl.sql.SqlLinkRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
public class SqlRepositoryIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private JdbcClient jdbcClient;

    private ChatRepository chatRepository;
    private LinkRepository linkRepository;

    private static final Long CHAT_ID = 1L;
    private static final String URL = "https://github.com/user/repo";

    @BeforeEach
    void setUp() {
        chatRepository = new SqlChatRepository(jdbcClient);
        linkRepository = new SqlLinkRepository(jdbcClient);

        jdbcClient.sql("DELETE FROM tracked_links").update();
        jdbcClient.sql("DELETE FROM chats").update();

        jdbcClient
                .sql("INSERT INTO chats (id) VALUES (:id)")
                .param("id", CHAT_ID)
                .update();
    }

    @Test
    void migrations_tablesExist() {
        Integer chatsCount = jdbcClient
                .sql("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'chats'")
                .query(Integer.class)
                .single();
        Integer linksCount = jdbcClient
                .sql("SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'tracked_links'")
                .query(Integer.class)
                .single();

        assertThat(chatsCount).isEqualTo(1);
        assertThat(linksCount).isEqualTo(1);
    }

    @Test
    void add_linkSavedToDatabase() {
        TrackedLink link = new TrackedLink(null, CHAT_ID, URL, Set.of("java"), Set.of("filter1"));

        TrackedLink saved = linkRepository.add(CHAT_ID, link);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUrl()).isEqualTo(URL);
        assertThat(saved.getTags()).isEqualTo(Set.of("java"));

        Optional<TrackedLink> found = linkRepository.find(CHAT_ID, URL);
        assertThat(found).isPresent();
        assertThat(found.get().getUrl()).isEqualTo(URL);
    }

    @Test
    void remove_linkAbsentAfterDeletion() {
        linkRepository.add(CHAT_ID, new TrackedLink(null, CHAT_ID, URL, Set.of(), Set.of()));

        linkRepository.remove(CHAT_ID, URL);

        Optional<TrackedLink> found = linkRepository.find(CHAT_ID, URL);
        assertThat(found).isEmpty();
    }

    @Test
    void add_duplicateLink_throwsException() {
        linkRepository.add(CHAT_ID, new TrackedLink(null, CHAT_ID, URL, Set.of(), Set.of()));

        assertThatThrownBy(() -> linkRepository.add(CHAT_ID, new TrackedLink(null, CHAT_ID, URL, Set.of(), Set.of())))
                .isInstanceOf(Exception.class);
    }

    @Test
    void findAll_returnsAllLinksForChat() {
        String url2 = "https://stackoverflow.com/questions/12345/title";
        linkRepository.add(CHAT_ID, new TrackedLink(null, CHAT_ID, URL, Set.of(), Set.of()));
        linkRepository.add(CHAT_ID, new TrackedLink(null, CHAT_ID, url2, Set.of(), Set.of()));

        List<TrackedLink> links = linkRepository.findAll(CHAT_ID);

        assertThat(links).hasSize(2);
        assertThat(links).extracting(TrackedLink::getUrl).containsExactlyInAnyOrder(URL, url2);
    }

    @Test
    void getAllLinksWithChats_returnsCorrectMapping() {
        Long chatId2 = 2L;
        jdbcClient
                .sql("INSERT INTO chats (id) VALUES (:id)")
                .param("id", chatId2)
                .update();
        linkRepository.add(CHAT_ID, new TrackedLink(null, CHAT_ID, URL, Set.of(), Set.of()));
        linkRepository.add(chatId2, new TrackedLink(null, CHAT_ID, URL, Set.of(), Set.of()));

        Map<String, List<Long>> result = linkRepository.getAllLinksWithChats();

        assertThat(result).containsKey(URL);
        assertThat(result.get(URL)).containsExactlyInAnyOrder(CHAT_ID, chatId2);
    }
}
