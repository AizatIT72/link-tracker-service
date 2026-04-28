package backend.academy.linktracker.bot.model;

/**
 * Состояния машины состояний (FSM) для диалога с пользователем.
 *
 * IDLE       — ожидание команды
 * WAIT_LINK  — ожидание ссылки после /track
 * WAIT_TAGS  — ожидание тегов после получения ссылки
 */
public enum UserState {
    IDLE,
    WAIT_LINK,
    WAIT_TAGS
}
