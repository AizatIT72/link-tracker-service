package backend.academy.linktracker.scrapper.repository.sql;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.sql.ResultSet;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
public class SqlLinkRepositoryImpl implements LinkRepository {

    private final JdbcClient jdbcClient;

    @Override
    @Transactional
    public Link save(Link link) {
        Long id = jdbcClient
                .sql("""
                INSERT INTO links (chat_id, url, last_checked_at, last_updated_at)
                VALUES (:chatId, :url, :lastCheckedAt, :lastUpdatedAt)
                RETURNING id
                """)
                .param("chatId", link.getChatId())
                .param("url", link.getUrl())
                .param("lastCheckedAt", link.getLastCheckedAt())
                .param("lastUpdatedAt", link.getLastUpdatedAt())
                .query(Long.class)
                .single();

        link.setId(id);

        if (link.getTags() != null) {
            for (String tag : link.getTags()) {
                jdbcClient
                        .sql("INSERT INTO link_tags (link_id, tag) VALUES (:linkId, :tag)")
                        .param("linkId", id)
                        .param("tag", tag)
                        .update();
            }
        }

        return link;
    }

    @Override
    @Transactional
    public void delete(long chatId, String url) {
        jdbcClient
                .sql("DELETE FROM links WHERE chat_id = :chatId AND url = :url")
                .param("chatId", chatId)
                .param("url", url)
                .update();
    }

    @Override
    public Optional<Link> findByChatIdAndUrl(long chatId, String url) {
        List<Link> links = query("WHERE l.chat_id = :chatId AND l.url = :url", Map.of("chatId", chatId, "url", url));
        return links.isEmpty() ? Optional.empty() : Optional.of(links.getFirst());
    }

    @Override
    public List<Link> findAllByChatId(long chatId) {
        return query("WHERE l.chat_id = :chatId", Map.of("chatId", chatId));
    }

    @Override
    public List<Link> findAll() {
        return query("", Map.of());
    }

    @Override
    public boolean existsByChatIdAndUrl(long chatId, String url) {
        return jdbcClient
                        .sql("SELECT COUNT(*) FROM links WHERE chat_id = :chatId AND url = :url")
                        .param("chatId", chatId)
                        .param("url", url)
                        .query(Long.class)
                        .single()
                > 0;
    }

    private List<Link> query(String where, Map<String, Object> params) {
        String sql = """
            SELECT l.id, l.chat_id, l.url, l.last_checked_at, l.last_updated_at, t.tag
            FROM links l
            LEFT JOIN link_tags t ON t.link_id = l.id
            """ + where + " ORDER BY l.id";

        Map<Long, Link> result = new LinkedHashMap<>();
        var spec = jdbcClient.sql(sql);
        for (var e : params.entrySet()) {
            spec = spec.param(e.getKey(), e.getValue());
        }
        spec.query((ResultSet rs) -> {
            long id = rs.getLong("id");
            if (!result.containsKey(id)) {
                result.put(
                        id,
                        Link.builder()
                                .id(id)
                                .chatId(rs.getLong("chat_id"))
                                .url(rs.getString("url"))
                                .lastCheckedAt(rs.getObject("last_checked_at", OffsetDateTime.class))
                                .lastUpdatedAt(rs.getObject("last_updated_at", OffsetDateTime.class))
                                .tags(new ArrayList<>())
                                .filters(new ArrayList<>())
                                .build());
            }
            String tag = rs.getString("tag");
            if (tag != null) {
                result.get(id).getTags().add(tag);
            }
        });

        return new ArrayList<>(result.values());
    }

    @Override
    public void update(Link link) {
        jdbcClient
                .sql("""
            UPDATE links
            SET last_checked_at = :lastCheckedAt,
                last_updated_at = :lastUpdatedAt
            WHERE id = :id
            """)
                .param("lastCheckedAt", link.getLastCheckedAt())
                .param("lastUpdatedAt", link.getLastUpdatedAt())
                .param("id", link.getId())
                .update();
    }
}
