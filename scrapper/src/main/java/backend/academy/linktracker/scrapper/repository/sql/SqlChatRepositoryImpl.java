package backend.academy.linktracker.scrapper.repository.sql;

import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.access-type", havingValue = "SQL")
public class SqlChatRepositoryImpl implements ChatRepository {

    private final JdbcClient jdbcClient;

    @Override
    public void save(Chat chat) {
        jdbcClient
                .sql("INSERT INTO chats (id) VALUES (:id) ON CONFLICT DO NOTHING")
                .param("id", chat.getId())
                .update();
    }

    @Override
    public void delete(long chatId) {
        jdbcClient.sql("DELETE FROM chats WHERE id = :id").param("id", chatId).update();
    }

    @Override
    public Optional<Chat> findById(long chatId) {
        return jdbcClient
                .sql("SELECT id FROM chats WHERE id = :id")
                .param("id", chatId)
                .query((rs, rowNum) -> new Chat(rs.getLong("id")))
                .optional();
    }

    @Override
    public boolean existsById(long chatId) {
        return jdbcClient
                        .sql("SELECT COUNT(*) FROM chats WHERE id = :id")
                        .param("id", chatId)
                        .query(Long.class)
                        .single()
                > 0;
    }
}
