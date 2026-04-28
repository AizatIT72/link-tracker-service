package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.service.TgChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tg-chat")
@RequiredArgsConstructor
@Slf4j
public class TgChatController {

    private final TgChatService tgChatService;

    /**
     * POST /tg-chat/{id} — зарегистрировать чат.
     * 200 — успешно, 409 — уже существует.
     */
    @PostMapping("/{id}")
    public ResponseEntity<Void> registerChat(@PathVariable long id) {
        log.atInfo().addKeyValue("chatId", id).log("Запрос на регистрацию чата");
        tgChatService.registerChat(id);
        return ResponseEntity.ok().build();
    }

    /**
     * DELETE /tg-chat/{id} — удалить чат.
     * 200 — успешно, 404 — не найден.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChat(@PathVariable long id) {
        log.atInfo().addKeyValue("chatId", id).log("Запрос на удаление чата");
        tgChatService.deleteChat(id);
        return ResponseEntity.ok().build();
    }
}
