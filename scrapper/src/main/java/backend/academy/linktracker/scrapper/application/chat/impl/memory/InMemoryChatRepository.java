package backend.academy.linktracker.scrapper.application.chat.impl.memory;

import backend.academy.linktracker.scrapper.application.chat.ChatRepository;
import backend.academy.linktracker.scrapper.application.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.application.exception.ChatNotFoundException;
import java.util.HashSet;
import java.util.Set;


public class InMemoryChatRepository implements ChatRepository {
    private final Set<Long> storage = new HashSet<>();

    @Override
    public void register(Long chatId) {
        if (storage.contains(chatId)) throw new ChatAlreadyExistsException(chatId);
        storage.add(chatId);
    }

    @Override
    public void delete(Long chatId) {
        if (!storage.contains(chatId)) throw new ChatNotFoundException(chatId);
        storage.remove(chatId);
    }

    @Override
    public boolean exists(Long chatId) {
        return storage.contains(chatId);
    }

    public void clear() {
        storage.clear();
    }
}
