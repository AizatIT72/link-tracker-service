package backend.academy.linktracker.scrapper.controller;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.ScrapperDto.AddLinkRequest;
import backend.academy.linktracker.scrapper.dto.ScrapperDto.LinkResponse;
import backend.academy.linktracker.scrapper.dto.ScrapperDto.ListLinksResponse;
import backend.academy.linktracker.scrapper.dto.ScrapperDto.RemoveLinkRequest;
import backend.academy.linktracker.scrapper.service.LinkService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST-контроллер для управления отслеживаемыми ссылками.
 * Соответствует scrapper-api.yaml: /links
 *
 * Все операции требуют заголовок Tg-Chat-Id.
 */
@RestController
@RequestMapping("/links")
@RequiredArgsConstructor
@Slf4j
public class LinksController {

    private final LinkService linkService;

    /**
     * GET /links — получить все отслеживаемые ссылки чата.
     * 200 — успешно, 404 — чат не найден.
     */
    @GetMapping
    public ResponseEntity<ListLinksResponse> getLinks(@RequestHeader("Tg-Chat-Id") long chatId) {
        log.atInfo().addKeyValue("chatId", chatId).log("Запрос списка ссылок");
        List<Link> links = linkService.getLinks(chatId);
        ListLinksResponse response = ListLinksResponse.builder()
                .links(links.stream().map(this::toDto).toList())
                .size(links.size())
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * POST /links — добавить ссылку в отслеживание.
     * 200 — успешно, 404 — чат не найден, 409 — ссылка уже существует.
     */
    @PostMapping
    public ResponseEntity<LinkResponse> addLink(
            @RequestHeader("Tg-Chat-Id") long chatId, @RequestBody AddLinkRequest request) {
        log.atInfo()
                .addKeyValue("chatId", chatId)
                .addKeyValue("url", request.getLink())
                .log("Запрос на добавление ссылки");
        Link link = linkService.addLink(chatId, request.getLink(), request.getTags(), request.getFilters());
        return ResponseEntity.ok(toDto(link));
    }

    /**
     * DELETE /links — убрать ссылку из отслеживания.
     * 200 — успешно, 404 — чат или ссылка не найдены.
     */
    @DeleteMapping
    public ResponseEntity<LinkResponse> removeLink(
            @RequestHeader("Tg-Chat-Id") long chatId, @RequestBody RemoveLinkRequest request) {
        log.atInfo()
                .addKeyValue("chatId", chatId)
                .addKeyValue("url", request.getLink())
                .log("Запрос на удаление ссылки");
        Link link = linkService.removeLink(chatId, request.getLink());
        return ResponseEntity.ok(toDto(link));
    }

    private LinkResponse toDto(Link link) {
        return LinkResponse.builder()
                .id(link.getId())
                .url(link.getUrl())
                .tags(link.getTags())
                .filters(link.getFilters())
                .build();
    }
}
