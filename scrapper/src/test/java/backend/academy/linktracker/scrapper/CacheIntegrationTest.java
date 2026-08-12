package backend.academy.linktracker.scrapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.application.Clearable;
import backend.academy.linktracker.scrapper.application.chat.ChatRepository;
import backend.academy.linktracker.scrapper.application.dto.request.AddLinkRequest;
import backend.academy.linktracker.scrapper.application.dto.request.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.application.dto.response.LinkResponse;
import backend.academy.linktracker.scrapper.application.dto.response.ListLinksResponse;
import backend.academy.linktracker.scrapper.application.link.LinkRepository;
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
    private ChatRepository chatRepository;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        if (chatRepository instanceof Clearable c) c.clear();
        if (linkRepository instanceof Clearable c) c.clear();
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());

        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Test
    void getLinks_shouldPopulateCache() throws Exception {
        registerChat(1L);
        addLink(1L, "https://github.com/user/repo");

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1L)).andExpect(status().isOk());

        Cache cache = cacheManager.getCache("Tg-Chat-Id");

        Cache.ValueWrapper wrapper = cache.get(1L);

        assertThat(wrapper).isNotNull();

        ListLinksResponse response = (ListLinksResponse) wrapper.get();

        assertThat(response.links()).extracting(LinkResponse::url).contains("https://github.com/user/repo");
    }

    @Test
    void getLinks_secondRequest_shouldUseCache() throws Exception {
        registerChat(1L);
        addLink(1L, "https://github.com/user/repo");

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1L)).andExpect(status().isOk());

        linkRepository.remove(1L, "https://github.com/user/repo");

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(1));
    }

    @Test
    void addLink_shouldEvictCache() throws Exception {
        registerChat(1L);
        addLink(1L, "https://github.com/user/repo");

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1L)).andExpect(status().isOk());

        Cache cache = cacheManager.getCache("Tg-Chat-Id");

        Cache.ValueWrapper wrapper = cache.get(1L);
        assertThat(wrapper).isNotNull();

        addLink(1L, "https://stackoverflow.com/q/123");

        assertThat(cache.get(1L)).isNull();
    }

    @Test
    void deleteLink_shouldEvictCache() throws Exception {
        registerChat(2L);
        addLink(2L, "https://github.com/user/repo");

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 2L)).andExpect(status().isOk());
        Cache cache = cacheManager.getCache("Tg-Chat-Id");

        Cache.ValueWrapper wrapper = cache.get(2L);
        assertThat(wrapper).isNotNull();

        RemoveLinkRequest req = new RemoveLinkRequest("https://github.com/user/repo");
        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        assertThat(cache.get(2L)).isNull();
    }

    @Test
    void cache_shouldExpireAfterTtl() throws Exception {
        registerChat(1L);
        addLink(1L, "https://github.com/user/repo");

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1L)).andExpect(status().isOk());

        Cache cache = cacheManager.getCache("Tg-Chat-Id");

        Cache.ValueWrapper wrapper = cache.get(1L);
        assertThat(wrapper).isNotNull();

        Thread.sleep(4000);

        wrapper = cache.get(1L);
        assertThat(wrapper).isNull();

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
