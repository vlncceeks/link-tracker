package backend.academy.linktracker.scrapper.application.linktag.impl.sql;

import backend.academy.linktracker.scrapper.application.linktag.LinkTagRepository;
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
        String sql = """
            SELECT tag_id
            FROM link_tags
            WHERE link_id = ?
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("tag_id"), linkId);
    }

    @Override
    public List<Integer> findLinkIdsByTagId(Integer tagId) {
        String sql = """
            SELECT link_id
            FROM link_tags
            WHERE tag_id = ?
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt("link_id"), tagId);
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
