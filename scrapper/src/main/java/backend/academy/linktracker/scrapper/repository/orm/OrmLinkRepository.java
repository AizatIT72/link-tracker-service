package backend.academy.linktracker.scrapper.repository.orm;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import backend.academy.linktracker.scrapper.repository.orm.entity.LinkEntity;
import backend.academy.linktracker.scrapper.repository.orm.entity.LinkTagEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.access-type", havingValue = "ORM")
public class OrmLinkRepository implements LinkRepository {

    private final LinkJpaRepository jpa;

    @Override
    @Transactional
    public Link save(Link link) {
        LinkEntity entity = LinkEntity.builder()
                .chatId(link.getChatId())
                .url(link.getUrl())
                .lastCheckedAt(link.getLastCheckedAt())
                .lastUpdatedAt(link.getLastUpdatedAt())
                .build();

        if (link.getTags() != null) {
            for (String tag : link.getTags()) {
                entity.getTags()
                        .add(LinkTagEntity.builder().link(entity).tag(tag).build());
            }
        }
        return toLink(jpa.save(entity));
    }

    @Override
    @Transactional
    public void delete(long chatId, String url) {
        jpa.deleteByChatIdAndUrl(chatId, url);
    }

    @Override
    public Optional<Link> findByChatIdAndUrl(long chatId, String url) {
        return jpa.findByChatIdAndUrl(chatId, url).map(this::toLink);
    }

    @Override
    public List<Link> findAllByChatId(long chatId) {
        return jpa.findAllByChatId(chatId).stream().map(this::toLink).toList();
    }

    @Override
    public List<Link> findAll() {
        return jpa.findAll().stream().map(this::toLink).toList();
    }

    @Override
    public boolean existsByChatIdAndUrl(long chatId, String url) {
        return jpa.existsByChatIdAndUrl(chatId, url);
    }

    @Override
    @Transactional
    public void update(Link link) {
        jpa.findById(link.getId()).ifPresent(entity -> {
            entity.setLastCheckedAt(link.getLastCheckedAt());
            entity.setLastUpdatedAt(link.getLastUpdatedAt());
            jpa.save(entity);
        });
    }

    private Link toLink(LinkEntity e) {
        List<String> tags = e.getTags() == null
                ? new ArrayList<>()
                : e.getTags().stream().map(LinkTagEntity::getTag).toList();
        return Link.builder()
                .id(e.getId())
                .chatId(e.getChatId())
                .url(e.getUrl())
                .lastCheckedAt(e.getLastCheckedAt())
                .lastUpdatedAt(e.getLastUpdatedAt())
                .tags(tags)
                .filters(new ArrayList<>())
                .build();
    }
}
