package backend.academy.linktracker.scrapper.service.notification.impl;

import backend.academy.linktracker.scrapper.client.BotClient;
import backend.academy.linktracker.scrapper.dto.LinkUpdate;
import backend.academy.linktracker.scrapper.service.notification.MessageSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class HttpMessageSender implements MessageSender {

    private final BotClient client;

    @Override
    public void sendMessage(LinkUpdate update) {
        log.atInfo().addKeyValue("id", update.getId()).log("Сообщение отправлено");
        client.sendUpdate(update);
    }
}
