package backend.academy.linktracker.scrapper.application.link.impl.orm;

import backend.academy.linktracker.scrapper.application.dto.response.LinkResponse;
import backend.academy.linktracker.scrapper.application.link.LinkRepository;
import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public TrackedLink update(TrackedLink link) {
        return repository.save(link);
    }

    @Override
    public List<TrackedLink> findAll(Long chatId) {
        List<TrackedLink> links = new ArrayList<>();
        int pageSize = 1000;
        int pageNumber = 0;

        Page<TrackedLink> page;
        do {
            page = repository.findAllByChatId(chatId, PageRequest.of(pageNumber, pageSize, Sort.by("id")));
            links.addAll(page.getContent());
            pageNumber++;
        } while (page.hasNext());

        return links;
    }

    @Override
    public Map<String, List<Long>> getAllLinksWithChats() {
        Map<String, List<Long>> result = new HashMap<>();
        int pageSize = 1000;
        int pageNumber = 0;

        Page<TrackedLink> page;
        do {
            page = repository.findAll(PageRequest.of(pageNumber, pageSize, Sort.by("id")));
            page.getContent().forEach(link -> result.computeIfAbsent(link.getUrl(), k -> new ArrayList<>())
                    .add(link.getChatId()));
            pageNumber++;
        } while (page.hasNext());

        return result;
    }

    @Override
    public List<LinkResponse> findAllWithTags(Long chatId) {
        Map<Integer, List<LinkWithTagRow>> grouped = new LinkedHashMap<>();
        int pageSize = 1000;
        int lastId = 0;

        while (true) {
            List<LinkWithTagRow> rows = repository.findAllWithTagsByChatIdKeySet(chatId, lastId, pageSize);

            if (rows.isEmpty()) break;

            rows.forEach(row ->
                    grouped.computeIfAbsent(row.id(), k -> new ArrayList<>()).add(row));

            lastId = rows.getLast().id();
            if (rows.size() < pageSize) break;
        }

        return grouped.entrySet().stream()
                .map(e -> {
                    List<LinkWithTagRow> rows = e.getValue();
                    List<String> tags = rows.stream()
                            .map(LinkWithTagRow::tagName)
                            .filter(Objects::nonNull)
                            .toList();
                    return new LinkResponse(e.getKey(), rows.getFirst().url(), tags, List.of());
                })
                .toList();
    }
}
