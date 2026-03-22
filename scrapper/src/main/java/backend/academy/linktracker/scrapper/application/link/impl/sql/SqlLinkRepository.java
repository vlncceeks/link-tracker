package backend.academy.linktracker.scrapper.application.link.impl.sql;

import backend.academy.linktracker.scrapper.application.link.LinkRepository;
import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
                INSERT INTO tracked_links (chat_id, url, tags, filters, last_checked_at)
                VALUES (:chatId, :url, :tags::text[], :filters::text[], :lastCheckedAt)
                RETURNING id, chat_id, url, tags, filters, last_checked_at
                """)
                .param("chatId", chatId)
                .param("url", link.getUrl())
                .param("tags", toArray(link.getTags()))
                .param("filters", toArray(link.getFilters()))
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

    // Маппер строки БД => TrackedLink
    @SuppressWarnings("PMD.UnusedFormalParameter")
    private TrackedLink mapRow(ResultSet rs, int rowNum) throws SQLException {
        Array tagsArray = rs.getArray("tags");
        Array filtersArray = rs.getArray("filters");

        Set<String> tags =
                tagsArray != null ? new HashSet<>(Arrays.asList((String[]) tagsArray.getArray())) : new HashSet<>();
        Set<String> filters = filtersArray != null
                ? new HashSet<>(Arrays.asList((String[]) filtersArray.getArray()))
                : new HashSet<>();

        TrackedLink link = new TrackedLink(rs.getInt("id"), rs.getLong("chat_id"), rs.getString("url"), tags, filters);

        OffsetDateTime lastCheckedAt = rs.getObject("last_checked_at", OffsetDateTime.class);
        link.setLastCheckedAt(lastCheckedAt != null ? lastCheckedAt.toInstant() : null);
        return link;
    }

    // Конвертация Set<String> => строка для PostgreSQL массива
    private String toArray(Set<String> set) {
        if (set == null || set.isEmpty()) return "{}";
        return "{" + set.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(",")) + "}";
    }
}
