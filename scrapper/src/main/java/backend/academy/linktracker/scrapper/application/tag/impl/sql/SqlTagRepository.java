package backend.academy.linktracker.scrapper.application.tag.impl.sql;

import backend.academy.linktracker.scrapper.application.tag.Tag;
import backend.academy.linktracker.scrapper.application.tag.TagRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@RequiredArgsConstructor
public class SqlTagRepository implements TagRepository {
    private final JdbcTemplate jdbcTemplate;

    private static final RowMapper<Tag> TAG_ROW_MAPPER = (rs, rowNum) -> new Tag(rs.getInt("id"), rs.getString("name"));

    @Override
    public Tag add(String name) {
        String sql = """
            INSERT INTO tags(name) VALUES (?)
            RETURNING id, name
        """;

        return jdbcTemplate.queryForObject(sql, TAG_ROW_MAPPER, name);
    }

    @Override
    public Tag update(String name, Tag tag) {
        String sql = "UPDATE tags SET name = ? WHERE id = ?";

        jdbcTemplate.update(sql, name, tag.getId());

        return new Tag(tag.getId(), name);
    }

    @Override
    public Optional<Tag> findById(Integer id) {
        String sql = "SELECT id, name FROM tags WHERE id = ?";

        List<Tag> result = jdbcTemplate.query(sql, TAG_ROW_MAPPER, id);

        return result.stream().findFirst();
    }

    @Override
    public Optional<Tag> findByName(String name) {
        String sql = "SELECT id, name FROM tags WHERE name = ?";

        List<Tag> result = jdbcTemplate.query(sql, TAG_ROW_MAPPER, name);

        return result.stream().findFirst();
    }

    @Override
    public List<Tag> findAll() {
        return jdbcTemplate.query("SELECT id, name FROM tags ORDER BY id", TAG_ROW_MAPPER);
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM tags WHERE id = ?";

        jdbcTemplate.update(sql, id);
    }
}
