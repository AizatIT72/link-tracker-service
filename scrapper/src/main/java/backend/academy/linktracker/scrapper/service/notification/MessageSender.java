package backend.academy.linktracker.scrapper.service.notification;

import backend.academy.linktracker.scrapper.dto.LinkUpdate;

public interface MessageSender {
    void sendMessage(LinkUpdate update);
}
