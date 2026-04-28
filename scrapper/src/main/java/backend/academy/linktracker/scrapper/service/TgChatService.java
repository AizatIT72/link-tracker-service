package backend.academy.linktracker.scrapper.service;

import backend.academy.linktracker.scrapper.domain.Chat;
import backend.academy.linktracker.scrapper.exception.ChatAlreadyExistsException;
import backend.academy.linktracker.scrapper.exception.ChatNotFoundException;
import backend.academy.linktracker.scrapper.repository.ChatRepository;
import backend.academy.linktracker.scrapper.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TgChatService {

    private final ChatRepository chatRepository;
    private final LinkRepository linkRepository;

    public void registerChat(long chatId) {
        if (chatRepository.existsById(chatId)) {
            throw new ChatAlreadyExistsException(chatId);
        }
        chatRepository.save(new Chat(chatId));
        log.atInfo().addKeyValue("chatId", chatId).log("Зарегистрирован новый чат");
    }

    public void deleteChat(long chatId) {
        if (!chatRepository.existsById(chatId)) {
            throw new ChatNotFoundException(chatId);
        }
        linkRepository.findAllByChatId(chatId).forEach(link -> linkRepository.delete(chatId, link.getUrl()));
        chatRepository.delete(chatId);
        log.atInfo().addKeyValue("chatId", chatId).log("Чат удалён");
    }

    public boolean existsById(long chatId) {
        return chatRepository.existsById(chatId);
    }
}
