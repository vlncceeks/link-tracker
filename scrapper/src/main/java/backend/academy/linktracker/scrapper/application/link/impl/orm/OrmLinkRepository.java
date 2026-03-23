package backend.academy.linktracker.scrapper.application.link.impl.orm;

import backend.academy.linktracker.scrapper.application.link.LinkRepository;
import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
public class OrmLinkRepository implements LinkRepository {
    private final LinkSpringRepository repository;

    @Override
    public TrackedLink add(Long chatId, TrackedLink link) {
        TrackedLink toSave = new TrackedLink(null, chatId, link.getUrl());
        return repository.save(toSave);
    }

    @Override
    public void remove(Long chatId, String url) {
        repository.deleteByChatIdAndUrl(chatId, url);
    }

    @Override
    public Optional<TrackedLink> find(Long chatId, String url) {
        return repository.findByChatIdAndUrl(chatId, url);
    }

    @Override
    public List<TrackedLink> findAll(Long chatId) {
        return repository.findAllByChatId(chatId);
    }

    @Override
    public Map<String, List<Long>> getAllLinksWithChats() {
        Map<String, List<Long>> result = new HashMap<>();
        repository.findAll().forEach(link -> result.computeIfAbsent(link.getUrl(), k -> new ArrayList<>())
                .add(link.getChatId()));
        return result;
    }
}
