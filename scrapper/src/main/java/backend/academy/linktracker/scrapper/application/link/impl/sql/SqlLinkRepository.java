package backend.academy.linktracker.scrapper.application.link.impl.sql;

import backend.academy.linktracker.scrapper.application.dto.response.LinkResponse;
import backend.academy.linktracker.scrapper.application.dto.response.LinksPage;
import backend.academy.linktracker.scrapper.application.link.LinkRepository;
import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import backend.academy.linktracker.scrapper.application.link.mapper.TrackedLinkRowMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static java.util.Optional.ofNullable;

@RequiredArgsConstructor
@Transactional
public class SqlLinkRepository implements LinkRepository {
    private final JdbcClient jdbcClient;
    private static final RowMapper<TrackedLink> ROW_MAPPER = new TrackedLinkRowMapper();

    @Override
    public TrackedLink add(Long chatId, TrackedLink link) {
        return jdbcClient
                .sql("""
                    INSERT INTO tracked_links (chat_id, url, last_checked_at)
                    VALUES (:chatId, :url, :lastCheckedAt)
                    RETURNING id, chat_id, url, last_checked_at
                    """)
                .param("chatId", chatId)
                .param("url", link.getUrl())
                .param(
                        "lastCheckedAt",
                        ofNullable(link.getLastCheckedAt())
                            .map(date -> OffsetDateTime.ofInstant(date, ZoneOffset.UTC))
                            .orElse(OffsetDateTime.now(ZoneOffset.UTC))
                )
                .query(ROW_MAPPER)
                .single();
    }

    @Override
    public void remove(Long chatId, String url) {
        jdbcClient
                .sql("DELETE FROM tracked_links WHERE chat_id = :chatId AND url = :url")
                .param("chatId", chatId)
                .param("url", url)
                .update();
    }

    @Override
    public Optional<TrackedLink> find(Long chatId, String url) {
        return jdbcClient
                .sql("SELECT * FROM tracked_links WHERE chat_id = :chatId AND url = :url")
                .param("chatId", chatId)
                .param("url", url)
                .query(ROW_MAPPER)
                .optional();
    }

    @Override
    public TrackedLink update(TrackedLink link) {
        return jdbcClient
                .sql("UPDATE tracked_links SET last_checked_at = :lastCheckedAt WHERE id = :linkId")
                .param("lastCheckedAt", link.getLastCheckedAt())
                .param("linkId", link.getId())
                .query(ROW_MAPPER)
                .single();
    }

    @Override
    public LinksPage getLinksWithChats(int limit, long lastId) {
        record Row(Long id, String url, Long chatId) {}
        Map<String, List<Long>> result = new LinkedHashMap<>();

        List<Row> rows = jdbcClient
                .sql("SELECT id, url, chat_id FROM tracked_links WHERE id > :lastId ORDER BY id LIMIT :limit")
                .param("lastId", lastId)
                .param("limit", limit)
                .query((rs, rowNum) -> new Row(rs.getLong("id"), rs.getString("url"), rs.getLong("chat_id")))
                .list();

        if (rows.isEmpty()) return new LinksPage(Map.of(), lastId);

        rows.forEach(
                row -> result.computeIfAbsent(row.url(), k -> new ArrayList<>()).add(row.chatId()));

        return new LinksPage(result, rows.getLast().id());
    }

    @Override
    public List<LinkResponse> findAllWithTags(Long chatId, int limit, long lastId) {
        record Row(int linkId, String url, String tagName) {}

        List<Row> rows = jdbcClient
                .sql("""
                SELECT tl.id, tl.url, t.name as tag_name
                FROM tracked_links tl
                LEFT JOIN link_tags lt ON tl.id = lt.link_id
                LEFT JOIN tags t ON lt.tag_id = t.id
                WHERE tl.chat_id = :chatId AND tl.id > :lastId
                ORDER BY tl.id
                LIMIT :limit
                """)
                .param("chatId", chatId)
                .param("lastId", lastId)
                .param("limit", limit)
                .query((rs, rowNum) -> new Row(rs.getInt("id"), rs.getString("url"), rs.getString("tag_name")))
                .list();

        Map<Integer, List<Row>> grouped = new LinkedHashMap<>();
        rows.forEach(row ->
                grouped.computeIfAbsent(row.linkId(), k -> new ArrayList<>()).add(row));

        return grouped.entrySet().stream()
                .map(e -> {
                    List<Row> linkRows = e.getValue();
                    List<String> tags = linkRows.stream()
                            .map(Row::tagName)
                            .filter(Objects::nonNull)
                            .toList();
                    return new LinkResponse(e.getKey(), linkRows.getFirst().url(), tags, List.of());
                })
                .toList();
    }
}
