package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.application.Clearable;
import backend.academy.linktracker.scrapper.application.chat.ChatRepository;
import backend.academy.linktracker.scrapper.application.dto.request.AddLinkRequest;
import backend.academy.linktracker.scrapper.application.dto.request.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.application.link.LinkRepository;
import backend.academy.linktracker.scrapper.infrastructure.service.LinkService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
        properties = {
            "app.access-type=MEMORY",
            "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration",
            "spring.cache.redis.time-to-live=3s"
        })
@ContextConfiguration(initializers = TestRedisConfiguration.class)
public class CacheIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @MockitoSpyBean
    private LinkService linkService;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private CacheManager cacheManager;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        if (chatRepository instanceof Clearable c) c.clear();
        if (linkRepository instanceof Clearable c) c.clear();
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
        Set<String> keys = redisTemplate.keys("rate-limit:*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    void getLinks_shouldPopulateCache() throws Exception {
        registerChat(1L);
        addLink(1L, "https://github.com/user/repo");

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1L)).andExpect(status().isOk());

        Set<String> keys = redisTemplate.keys("Tg-Chat-Id::*");
        assertThat(keys).isNotEmpty();

        String cachedValue = redisTemplate.opsForValue().get(keys.iterator().next());
        assertThat(cachedValue).isNotNull();
        assertThatCode(() -> objectMapper.readTree(cachedValue)).doesNotThrowAnyException();
        assertThat(cachedValue).contains("github.com/user/repo");
    }

    @Test
    void getLinks_secondRequest_shouldHitCache() throws Exception {
        registerChat(1L);
        addLink(1L, "https://github.com/user/repo");

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1L)).andExpect(status().isOk());
        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1L)).andExpect(status().isOk());

        verify(linkService, times(1)).getAllByChatId(1L);
    }

    @Test
    void addLink_shouldEvictCache() throws Exception {
        registerChat(1L);
        addLink(1L, "https://github.com/user/repo");

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1L)).andExpect(status().isOk());
        assertThat(redisTemplate.keys("Tg-Chat-Id::*")).isNotEmpty();

        addLink(1L, "https://stackoverflow.com/q/123");

        assertThat(redisTemplate.keys("Tg-Chat-Id::*")).isEmpty();
    }

    @Test
    void deleteLink_shouldEvictCache() throws Exception {
        registerChat(1L);
        addLink(1L, "https://github.com/user/repo");

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1L)).andExpect(status().isOk());
        assertThat(redisTemplate.keys("Tg-Chat-Id::*")).isNotEmpty();

        RemoveLinkRequest req = new RemoveLinkRequest("https://github.com/user/repo");
        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        Cache cache = cacheManager.getCache("Tg-Chat-Id");
        assertThat(cache.get(1L)).isNull();
    }

    @Test
    void cache_shouldExpireAfterTtl() throws Exception {
        registerChat(1L);
        addLink(1L, "https://github.com/user/repo");

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1L)).andExpect(status().isOk());
        assertThat(redisTemplate.keys("Tg-Chat-Id::*")).isNotEmpty();

        Thread.sleep(4000);

        assertThat(redisTemplate.keys("Tg-Chat-Id::*")).isEmpty();

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1L)).andExpect(status().isOk());
    }

    private void registerChat(long chatId) throws Exception {
        mockMvc.perform(post("/tg-chat/" + chatId)).andExpect(status().isOk());
    }

    private void addLink(long chatId, String url) throws Exception {
        AddLinkRequest req = new AddLinkRequest(url, List.of(), List.of());
        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }
}
