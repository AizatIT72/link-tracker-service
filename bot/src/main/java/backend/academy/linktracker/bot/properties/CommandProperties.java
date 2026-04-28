package backend.academy.linktracker.bot.properties;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.telegram.commands")
@Getter
@Setter
public class CommandProperties {

    @NotEmpty
    private String startMessage;

    @NotEmpty
    private String helpMessage;

    @NotEmpty
    private String unknownCommandMessage;
}
