package backend.academy.linktracker.bot;

import backend.academy.linktracker.bot.application.dto.request.LinkUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.pengrad.telegrambot.TelegramBot;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UpdateControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private TelegramBot telegramBot;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }


    @Test
    void receiveUpdate_validRequest_returns200() throws Exception {
        LinkUpdateRequest request = new LinkUpdateRequest(
            1L,
            "https://github.com/user/repo",
            "Новый коммит",
            List.of(123L, 456L)
        );

        mockMvc.perform(post("/updates")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    void receiveUpdate_emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/updates")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void receiveUpdate_invalidJson_returns400() throws Exception {
        mockMvc.perform(post("/updates")
                .contentType(MediaType.APPLICATION_JSON)
                .content("not-a-json"))
            .andExpect(status().isBadRequest());
    }
}
