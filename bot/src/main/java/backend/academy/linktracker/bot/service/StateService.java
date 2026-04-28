package backend.academy.linktracker.bot.service;

import backend.academy.linktracker.bot.model.UserContext;
import backend.academy.linktracker.bot.model.UserState;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/**
 * Сервис управления состояниями пользователей.
 * Хранит контекст диалога каждого пользователя в памяти (ConcurrentHashMap).
 */
@Service
public class StateService {

    private final ConcurrentHashMap<Long, UserContext> userContextMap = new ConcurrentHashMap<>();

    public UserContext getOrCreate(long chatId) {
        return userContextMap.computeIfAbsent(chatId, id -> new UserContext());
    }

    public UserState getState(long chatId) {
        return getOrCreate(chatId).getState();
    }

    public void setState(long chatId, UserState state) {
        getOrCreate(chatId).setState(state);
    }

    public void setPendingLink(long chatId, String link) {
        getOrCreate(chatId).setPendingLink(link);
    }

    public String getPendingLink(long chatId) {
        return getOrCreate(chatId).getPendingLink();
    }

    public void reset(long chatId) {
        getOrCreate(chatId).reset();
    }
}
