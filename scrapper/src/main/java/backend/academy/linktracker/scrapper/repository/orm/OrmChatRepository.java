package backend.academy.linktracker.scrapper.repository.orm;

import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.orm.entity.ChatEntity;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
public class OrmChatRepository implements ChatRepository {

    private final ChatJpaRepository jpa;

    @Override
    public void save(Chat chat) {
        jpa.save(new ChatEntity(chat.getId()));
    }

    @Override
    public void delete(long chatId) {
        jpa.deleteById(chatId);
    }

    @Override
    public Optional<Chat> findById(long chatId) {
        return jpa.findById(chatId).map(e -> new Chat(e.getId()));
    }

    @Override
    public boolean existsById(long chatId) {
        return jpa.existsById(chatId);
    }
}
