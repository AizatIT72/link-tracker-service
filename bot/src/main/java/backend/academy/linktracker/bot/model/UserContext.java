package backend.academy.linktracker.bot.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Контекст пользователя в машине состояний.
 * Хранится в памяти, содержит текущее состояние и промежуточные данные диалога.
 */
@Getter
@Setter
public class UserContext {

    private UserState state = UserState.IDLE;

    /** Ссылка, которую пользователь хочет отслеживать (заполняется на шаге WAIT_LINK) */
    private String pendingLink;

    /** Теги, введённые пользователем (заполняются на шаге WAIT_TAGS) */
    private List<String> pendingTags;

    public void reset() {
        this.state = UserState.IDLE;
        this.pendingLink = null;
        this.pendingTags = null;
    }
}
