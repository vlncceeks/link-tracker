package backend.academy.linktracker.scrapper;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import backend.academy.linktracker.scrapper.properties.application.chat.impl.InMemoryChatRepository;
import backend.academy.linktracker.scrapper.properties.application.dto.request.AddLinkRequest;
import backend.academy.linktracker.scrapper.properties.application.dto.request.RemoveLinkRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ScrapperControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InMemoryChatRepository chatRepository;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        chatRepository.clear();
    }

    private static final String TEST_URL = "https://github.com/user/repo";

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

    @Test
    void addAndGetLink_returnsAddedLink() throws Exception {
        registerChat(1L);
        addLink(1L, TEST_URL);

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links[*].url", hasItem(TEST_URL)));
    }

    @Test
    void addAndDeleteLink_linkAbsentAfterDeletion() throws Exception {
        registerChat(1L);
        addLink(1L, TEST_URL);

        RemoveLinkRequest removeReq = new RemoveLinkRequest(TEST_URL);
        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(removeReq)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links[*].url", not(hasItem(TEST_URL))));
    }

    @Test
    void deleteLink_fromNonExistentChat_returnsErrorAndLinkSurvives() throws Exception {
        registerChat(1L);
        addLink(1L, TEST_URL);

        RemoveLinkRequest removeReq = new RemoveLinkRequest(TEST_URL);
        mockMvc.perform(delete("/links")
                        .header("Tg-Chat-Id", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(removeReq)))
                .andExpect(status().is4xxClientError());

        mockMvc.perform(get("/links").header("Tg-Chat-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.links[*].url", hasItem(TEST_URL)));
    }

    @Test
    void addLink_toNonExistentChat_returnsError() throws Exception {
        registerChat(1L);

        AddLinkRequest req = new AddLinkRequest(TEST_URL, List.of(), List.of());
        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void addLink_toDeletedChat_returnsError() throws Exception {
        registerChat(1L);

        mockMvc.perform(delete("/tg-chat/1")).andExpect(status().isOk());

        AddLinkRequest req = new AddLinkRequest(TEST_URL, List.of(), List.of());
        mockMvc.perform(post("/links")
                        .header("Tg-Chat-Id", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void deleteChat_nonExistent_returns404() throws Exception {
        mockMvc.perform(delete("/tg-chat/1")).andExpect(status().isNotFound());
    }
}
