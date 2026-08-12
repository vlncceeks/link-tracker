package backend.academy.linktracker.scrapper.infrastructure.service;

import backend.academy.linktracker.scrapper.application.chat.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {
    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private final ChatRepository chatRepository;

    public void register(Long id) {
        if (chatRepository.exists(id)) {
            logger.atInfo().addKeyValue("chatId", id).log("Chat already registered");
            return;
        }
        logger.atInfo().addKeyValue("chatId", id).log("Chat registration");
        chatRepository.register(id);
    }

    public void delete(Long id) {
        logger.atInfo().addKeyValue("chatId", id).log("Delete chat");
        chatRepository.delete(id);
    }

    public boolean exists(Long id) {
        return chatRepository.exists(id);
    }
}
