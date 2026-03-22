package backend.academy.linktracker.scrapper.application.chat.impl.orm;

import backend.academy.linktracker.scrapper.application.chat.ChatRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrmChatRepository implements ChatRepository {
    private final ChatSpringRepository repository;

    @Override
    public void register(Long chatId) {
        repository.save(new Chat(chatId));
    }

    @Override
    public void delete(Long chatId) {
        repository.deleteById(chatId);
    }

    @Override
    public boolean exists(Long chatId) {
        return repository.existsById(chatId);
    }
}
