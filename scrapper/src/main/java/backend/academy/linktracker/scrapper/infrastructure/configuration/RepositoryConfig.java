package backend.academy.linktracker.scrapper.infrastructure.configuration;

import backend.academy.linktracker.scrapper.application.chat.ChatRepository;
import backend.academy.linktracker.scrapper.application.chat.impl.memory.InMemoryChatRepository;
import backend.academy.linktracker.scrapper.application.chat.impl.orm.ChatSpringRepository;
import backend.academy.linktracker.scrapper.application.chat.impl.orm.OrmChatRepository;
import backend.academy.linktracker.scrapper.application.chat.impl.sql.SqlChatRepository;
import backend.academy.linktracker.scrapper.application.link.LinkRepository;
import backend.academy.linktracker.scrapper.application.link.impl.memory.InMemoryLinkRepository;
import backend.academy.linktracker.scrapper.application.link.impl.orm.LinkSpringRepository;
import backend.academy.linktracker.scrapper.application.link.impl.orm.OrmLinkRepository;
import backend.academy.linktracker.scrapper.application.link.impl.sql.SqlLinkRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

@Configuration
public class RepositoryConfig {
    @Bean
    @ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
    public ChatRepository sqlChatRepository(JdbcClient jdbcClient) {
        System.out.println("=== CREATING SQL CHAT REPOSITORY ===");
        return new SqlChatRepository(jdbcClient);
    }

    @Bean
    @ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
    public ChatRepository ormChatRepository(ChatSpringRepository chatSpringRepository) {
        return new OrmChatRepository(chatSpringRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "app.access-type", havingValue = "MEMORY")
    public ChatRepository inMemoryChatRepository() {
        System.out.println("=== CREATING IN MEMORY CHAT REPOSITORY ===");
        return new InMemoryChatRepository();
    }

    @Bean
    @ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
    public LinkRepository sqlLinkRepository(JdbcClient jdbcClient) {
        return new SqlLinkRepository(jdbcClient);
    }

    @Bean
    @ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
    public LinkRepository ormLinkRepository(LinkSpringRepository linkSpringRepository) {
        return new OrmLinkRepository(linkSpringRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "app.access-type", havingValue = "MEMORY")
    public LinkRepository inMemoryLinkRepository(ChatRepository chatRepository) {
        return new InMemoryLinkRepository(chatRepository);
    }
}
