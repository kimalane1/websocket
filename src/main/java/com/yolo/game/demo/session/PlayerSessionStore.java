package com.yolo.game.demo.session;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PlayerSessionStore {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    public void add(String nickname, WebSocketSession session) {
        sessions.put(nickname, session);
    }

    public WebSocketSession getSessionByNickname(String nickname) {
        return sessions.get(nickname);
    }

    public Collection<WebSocketSession> getAllSessions() {
        return sessions.values();
    }

    public void removeBySession(WebSocketSession session) {
        sessions.values().removeIf(s -> s.getId().equals(session.getId()));
    }
}
