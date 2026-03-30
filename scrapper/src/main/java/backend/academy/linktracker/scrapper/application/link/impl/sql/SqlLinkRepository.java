package backend.academy.linktracker.scrapper.application.link.impl.sql;

import backend.academy.linktracker.scrapper.application.dto.response.LinkResponse;
import backend.academy.linktracker.scrapper.application.link.LinkRepository;
import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional
public class SqlLinkRepository implements LinkRepository {
    private final JdbcClient jdbcClient;

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
                        link.getLastCheckedAt() != null
                                ? OffsetDateTime.ofInstant(link.getLastCheckedAt(), ZoneOffset.UTC)
                                : OffsetDateTime.now(ZoneOffset.UTC))
                .query(this::mapRow)
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
                .query(this::mapRow)
                .optional();
    }

    @Override
    public List<TrackedLink> findAll(Long chatId) {
        return jdbcClient
                .sql("SELECT * FROM tracked_links WHERE chat_id = :chatId")
                .param("chatId", chatId)
                .query(this::mapRow)
                .list();
    }

    @Override
    public Map<String, List<Long>> getAllLinksWithChats() {
        record Row(String url, Long chatId) {}

        List<Row> rows = jdbcClient
                .sql("SELECT url, chat_id FROM tracked_links")
                .query((rs, rowNum) -> new Row(rs.getString("url"), rs.getLong("chat_id")))
                .list();

        Map<String, List<Long>> result = new HashMap<>();
        rows.forEach(
                row -> result.computeIfAbsent(row.url(), k -> new ArrayList<>()).add(row.chatId()));
        return result;
    }

    @Override
    public List<LinkResponse> findAllWithTags(Long chatId) {
        record Row(int linkId, String url, String tagName) {}

        List<Row> rows = jdbcClient
                .sql("""
            SELECT tl.id, tl.url, t.name as tag_name
            FROM tracked_links tl
            LEFT JOIN link_tags lt ON tl.id = lt.link_id
            LEFT JOIN tags t ON lt.tag_id = t.id
            WHERE tl.chat_id = :chatId
            """)
                .param("chatId", chatId)
                .query((rs, rowNum) -> new Row(rs.getInt("id"), rs.getString("url"), rs.getString("tag_name")))
                .list();

        return rows.stream()
                .collect(Collectors.groupingBy(Row::linkId, LinkedHashMap::new, Collectors.toList()))
                .entrySet()
                .stream()
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

    // Маппер строки БД => TrackedLink
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private TrackedLink mapRow(ResultSet rs, int rowNum) throws SQLException {
        TrackedLink link = new TrackedLink(rs.getInt("id"), rs.getLong("chat_id"), rs.getString("url"));

        OffsetDateTime lastCheckedAt = rs.getObject("last_checked_at", OffsetDateTime.class);
        link.setLastCheckedAt(lastCheckedAt != null ? lastCheckedAt.toInstant() : null);
        return link;
    }
}
