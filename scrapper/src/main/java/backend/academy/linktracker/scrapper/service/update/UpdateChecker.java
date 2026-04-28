package backend.academy.linktracker.scrapper.service.update;

import backend.academy.linktracker.scrapper.domain.Link;
import backend.academy.linktracker.scrapper.dto.UpdateNotification;
import java.util.List;

public interface UpdateChecker {

    boolean supports(String url);

    List<UpdateNotification> checkForUpdates(Link link);
}
