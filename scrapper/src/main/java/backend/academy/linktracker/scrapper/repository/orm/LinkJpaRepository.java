package backend.academy.linktracker.scrapper.repository.orm;

import backend.academy.linktracker.scrapper.repository.orm.entity.LinkEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
public interface LinkJpaRepository extends JpaRepository<LinkEntity, Long> {

    Optional<LinkEntity> findByChatIdAndUrl(long chatId, String url);

    List<LinkEntity> findAllByChatId(long chatId);

    boolean existsByChatIdAndUrl(long chatId, String url);

    @Transactional
    void deleteByChatIdAndUrl(long chatId, String url);
}
