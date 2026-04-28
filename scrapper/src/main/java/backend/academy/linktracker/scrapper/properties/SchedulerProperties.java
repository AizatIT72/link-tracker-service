package backend.academy.linktracker.scrapper.properties;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

@ConfigurationProperties(prefix = "app.scheduler")
@Getter
@Setter
public class SchedulerProperties {

    @DurationUnit(ChronoUnit.MILLIS)
    private Duration interval = Duration.ofMinutes(1);

    private int previewLength;
}
