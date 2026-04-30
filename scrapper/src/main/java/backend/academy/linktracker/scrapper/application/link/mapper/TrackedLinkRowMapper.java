package backend.academy.linktracker.scrapper.application.link.mapper;

import backend.academy.linktracker.scrapper.application.link.TrackedLink;
import org.springframework.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

public class TrackedLinkRowMapper implements RowMapper<TrackedLink> {

    @Override
    public TrackedLink mapRow(ResultSet rs, int rowNum) throws SQLException {
        TrackedLink link = new TrackedLink(
            rs.getInt("id"),
            rs.getLong("chat_id"),
            rs.getString("url")
        );

        OffsetDateTime lastCheckedAt = rs.getObject("last_checked_at", OffsetDateTime.class);
        link.setLastCheckedAt(lastCheckedAt != null ? lastCheckedAt.toInstant() : null);
        return link;
    }
}
