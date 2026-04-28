package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.domain.Chat;
import java.util.Optional;

public interface ChatRepository {

    void save(Chat chat);

    void delete(long chatId);

    Optional<Chat> findById(long chatId);

    boolean existsById(long chatId);
}
