package backend.academy.linktracker.scrapper.application.link.impl.orm;

import backend.academy.linktracker.scrapper.application.dto.response.LinkResponse;
import backend.academy.linktracker.scrapper.application.dto.response.LinksPage;
import backend.academy.linktracker.scrapper.application.link.LinkRepository;
import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
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

    @Transactional
    @Override
    public void remove(Long chatId, String url) {
        repository.deleteByChatIdAndUrl(chatId, url);
    }

    @Override
    public Optional<TrackedLink> find(Long chatId, String url) {
        return repository.findByChatIdAndUrl(chatId, url);
    }

    @Override
    public TrackedLink update(TrackedLink link) {
        return repository.save(link);
    }

    @Override
    public LinksPage getLinksWithChats(int limit, long lastId) {
        Map<String, List<Long>> result = new LinkedHashMap<>();

        List<TrackedLink> rows = repository.findByIdGreaterThanOrderByIdAsc(lastId, PageRequest.of(0, limit));

        if (rows.isEmpty()) return new LinksPage(Map.of(), lastId);

        rows.forEach(link ->
                result.computeIfAbsent(link.getUrl(), k -> new ArrayList<>()).add(link.getChatId()));

        return new LinksPage(result, rows.getLast().getId());
    }

    @Override
    public List<LinkResponse> findAllWithTags(Long chatId, int limit, long lastId) {
        List<LinkWithTagRow> rows = repository.findAllWithTagsByChatIdKeySet(chatId, lastId, limit);

        Map<Integer, List<LinkWithTagRow>> grouped = new LinkedHashMap<>();
        rows.forEach(
                row -> grouped.computeIfAbsent(row.id(), k -> new ArrayList<>()).add(row));

        return grouped.entrySet().stream()
                .map(e -> {
                    List<LinkWithTagRow> linkRows = e.getValue();
                    List<String> tags = linkRows.stream()
                            .map(LinkWithTagRow::tagName)
                            .filter(Objects::nonNull)
                            .toList();
                    return new LinkResponse(e.getKey(), linkRows.getFirst().url(), tags, List.of());
                })
                .toList();
    }
}
