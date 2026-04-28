package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.exception.LinkAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.LinkNotFoundException;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LinkService {

    private final LinkRepository linkRepository;
    private final ChatRepository chatRepository;

    public List<Link> getLinks(long chatId) {
        validateChatExists(chatId);
        return linkRepository.findAllByChatId(chatId);
    }

    public Link addLink(long chatId, String url, List<String> tags, List<String> filters) {
        validateChatExists(chatId);
        if (linkRepository.existsByChatIdAndUrl(chatId, url)) {
            throw new LinkAlreadyExistsException(url);
        }

        Link link = Link.builder()
                .chatId(chatId)
                .url(url)
                .tags(tags != null ? tags : List.of())
                .filters(filters != null ? filters : List.of())
                .lastCheckedAt(OffsetDateTime.now())
                .lastUpdatedAt(OffsetDateTime.now().minusYears(1))
                .build();

        Link saved = linkRepository.save(link);
        log.atInfo().addKeyValue("chatId", chatId).addKeyValue("url", url).log("Ссылка добавлена");
        return saved;
    }

    public Link removeLink(long chatId, String url) {
        validateChatExists(chatId);
        Link existing =
                linkRepository.findByChatIdAndUrl(chatId, url).orElseThrow(() -> new LinkNotFoundException(url));
        linkRepository.delete(chatId, url);
        log.atInfo().addKeyValue("chatId", chatId).addKeyValue("url", url).log("Ссылка удалена");
        return existing;
    }

    public List<Link> getAllLinks() {
        return linkRepository.findAll();
    }

    private void validateChatExists(long chatId) {
        if (!chatRepository.existsById(chatId)) {
            throw new ChatNotFoundException(chatId);
        }
    }
}
