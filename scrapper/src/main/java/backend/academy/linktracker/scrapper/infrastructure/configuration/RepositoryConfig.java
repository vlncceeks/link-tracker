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
import backend.academy.linktracker.scrapper.application.linktag.LinkTagRepository;
import backend.academy.linktracker.scrapper.application.linktag.impl.memory.InMemoryLinkTagRepository;
import backend.academy.linktracker.scrapper.application.linktag.impl.orm.LinkTagSpringRepository;
import backend.academy.linktracker.scrapper.application.linktag.impl.orm.OrmLinkTagRepository;
import backend.academy.linktracker.scrapper.application.linktag.impl.sql.SqlLinkTagRepository;
import backend.academy.linktracker.scrapper.application.tag.TagRepository;
import backend.academy.linktracker.scrapper.application.tag.impl.memory.InMemoryTagRepository;
import backend.academy.linktracker.scrapper.application.tag.impl.orm.OrmTagRepository;
import backend.academy.linktracker.scrapper.application.tag.impl.orm.TagSpringRepository;
import backend.academy.linktracker.scrapper.application.tag.impl.sql.SqlTagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

@Configuration
public class RepositoryConfig {
    private static final Logger logger = LoggerFactory.getLogger(RepositoryConfig.class);

    @Bean
    @ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
    public ChatRepository sqlChatRepository(JdbcClient jdbcClient) {
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
        return new InMemoryLinkRepository();
    }

    @Bean
    @ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
    public LinkTagRepository sqlLinkTagRepository(JdbcTemplate jdbcTemplate) {
        return new SqlLinkTagRepository(jdbcTemplate);
    }

    @Bean
    @ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
    public LinkTagRepository ormLinkTagRepository(LinkTagSpringRepository linkTagSpringRepository) {
        return new OrmLinkTagRepository(linkTagSpringRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "app.access-type", havingValue = "MEMORY")
    public LinkTagRepository inMemoryLinkTagRepository() {
        return new InMemoryLinkTagRepository();
    }

    @Bean
    @ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
    public TagRepository sqlTagRepository(JdbcTemplate jdbcTemplate) {
        return new SqlTagRepository(jdbcTemplate);
    }

    @Bean
    @ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
    public TagRepository ormTagRepository(TagSpringRepository tagSpringRepository) {
        return new OrmTagRepository(tagSpringRepository);
    }

    @Bean
    @ConditionalOnProperty(name = "app.access-type", havingValue = "MEMORY")
    public TagRepository inMemoryTagRepository() {
        return new InMemoryTagRepository();
    }
}
