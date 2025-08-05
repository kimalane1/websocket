package com.yolo.game.demo.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolo.game.demo.model.BetRequest;
import com.yolo.game.demo.service.GameService;
import com.yolo.game.demo.session.PlayerSessionStore;
import com.yolo.game.demo.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.Optional;

@Component
public class GameWebSocketHandler implements WebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(GameWebSocketHandler.class);
    private final ObjectMapper objectMapper;
    private final ValidationUtil validationUtil;
    private final PlayerSessionStore playerSessionStore;
    private final GameService gameService;

    public GameWebSocketHandler(ValidationUtil validationUtil,
                                PlayerSessionStore playerSessionStore,
                                ObjectMapper objectMapper,
                                GameService gameService) {

        this.validationUtil = validationUtil;
        this.playerSessionStore = playerSessionStore;
        this.objectMapper = objectMapper;
        this.gameService = gameService;
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        log.info("established connection {}", session.getId());
    }

    @Override
    public void handleMessage(@NonNull WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String payload = (String) message.getPayload();
        try {
            BetRequest bet = objectMapper.readValue(payload, BetRequest.class);

            Optional<String> error = validationUtil.getValidationErrorMessage(bet);
            if (error.isPresent()) {
                session.sendMessage(new TextMessage("Invalid bet request: " + error.get()));
                return;
            }

            playerSessionStore.add(bet.nick(), session);
            gameService.receive(bet);

            session.sendMessage(new TextMessage("Bet accepted: " + bet.nick()));
        } catch (Exception e) {
            session.sendMessage(new TextMessage("Error: incorrect bet request due to " + e.getMessage()));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn("Error {}: {}", session.getId(), exception.getMessage());
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus closeStatus) {
        playerSessionStore.removeBySession(session);
        log.info("client disconnected {}", session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
}
