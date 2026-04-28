package backend.academy.linktracker.scrapper.repository.orm;

import backend.academy.linktracker.scrapper.repository.orm.entity.ChatEntity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;

@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
public interface ChatJpaRepository extends JpaRepository<ChatEntity, Long> {}
