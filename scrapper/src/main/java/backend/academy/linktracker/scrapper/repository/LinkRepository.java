package backend.academy.linktracker.scrapper.repository;

import backend.academy.linktracker.scrapper.domain.Link;
import java.util.List;
import java.util.Optional;

public interface LinkRepository {

    Link save(Link link);

    void delete(long chatId, String url);

    Optional<Link> findByChatIdAndUrl(long chatId, String url);

    List<Link> findAllByChatId(long chatId);

    List<Link> findAll();

    boolean existsByChatIdAndUrl(long chatId, String url);

    void update(Link link);
}
