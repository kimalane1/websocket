package com.yolo.game.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolo.game.demo.session.PlayerSessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Service
public class NotificationService {
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final PlayerSessionStore playerSessionStore;

    public NotificationService(PlayerSessionStore playerSessionStore) {
        this.playerSessionStore = playerSessionStore;
    }

    public void broadcast(Object payload) {
        playerSessionStore.getAllSessions().forEach(session -> {
            sendToSession(session, payload);
        });
    }

    public void sendToPlayer(String nick, Object payload) {
        WebSocketSession session = playerSessionStore.getSessionByNickname(nick);
        sendToSession(session, payload);
    }

    private void sendToSession(WebSocketSession session, Object payload) {
        if (session == null || !session.isOpen()) return;
        try {
            String json = new ObjectMapper().writeValueAsString(payload);
            session.sendMessage(new TextMessage(json));
        } catch (IOException ex) {
            log.warn("Failed to send message to session {}: {}", session.getId(), ex.getMessage());
        }
    }
}
