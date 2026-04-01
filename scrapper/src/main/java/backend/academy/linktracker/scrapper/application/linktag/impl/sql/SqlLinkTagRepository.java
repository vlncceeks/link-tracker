package backend.academy.linktracker.scrapper.application.linktag.impl.sql;

import backend.academy.linktracker.scrapper.application.linktag.LinkTagRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

@RequiredArgsConstructor
public class SqlLinkTagRepository implements LinkTagRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void addTagToLink(Integer linkId, Integer tagId) {
        String sql = """
            INSERT INTO link_tags(link_id, tag_id)
            VALUES (?, ?)
            ON CONFLICT (link_id, tag_id) DO NOTHING
        """;

        jdbcTemplate.update(sql, linkId, tagId);
    }

    @Override
    public void removeTagFromLink(Integer linkId, Integer tagId) {
        String sql = """
            DELETE FROM link_tags
            WHERE link_id = ? AND tag_id = ?
        """;

        jdbcTemplate.update(sql, linkId, tagId);
    }

    @Override
    public List<Integer> findTagIdsByLinkId(Integer linkId) {
        List<Integer> ids = new ArrayList<>();
        int pageSize = 1000;
        long lastId = 0;
        String sql = "SELECT id, tag_id FROM link_tags WHERE link_id = ? AND id > ? ORDER BY id LIMIT ?";

        while (true) {
            List<int[]> page = jdbcTemplate.query(
                    sql,
                    (rs, rowNum) -> new int[] {(int) rs.getLong("id"), rs.getInt("tag_id")},
                    linkId,
                    lastId,
                    pageSize);

            if (page.isEmpty()) break;
            page.forEach(row -> ids.add(row[1]));
            lastId = page.getLast()[0];
            if (page.size() < pageSize) break;
        }

        return ids;
    }

    @Override
    public List<Integer> findLinkIdsByTagId(Integer tagId) {
        List<Integer> ids = new ArrayList<>();
        int pageSize = 1000;
        long lastId = 0;
        String sql = "SELECT id, link_id FROM link_tags WHERE tag_id = ? AND id > ? ORDER BY id LIMIT ?";

        while (true) {
            List<int[]> page = jdbcTemplate.query(
                    sql,
                    (rs, rowNum) -> new int[] {(int) rs.getLong("id"), rs.getInt("link_id")},
                    tagId,
                    lastId,
                    pageSize);

            if (page.isEmpty()) break;
            page.forEach(row -> ids.add(row[1]));
            lastId = page.getLast()[0];
            if (page.size() < pageSize) break;
        }

        return ids;
    }

    @Override
    public void removeAllTagsFromLink(Integer linkId) {
        String sql = """
            DELETE FROM link_tags
            WHERE link_id = ?
        """;

        jdbcTemplate.update(sql, linkId);
    }
}
