package com.yolo.game.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolo.game.demo.model.BetRequest;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.math.BigDecimal;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class WebSocketTestClient {
    private final int port;
    private final ObjectMapper objectMapper;
    private WebSocketSession session;
    private final CompletableFuture<String> firstMessage = new CompletableFuture<>();
    private final CompletableFuture<String> secondMessage = new CompletableFuture<>();

    public WebSocketTestClient(int port, ObjectMapper objectMapper) {
        this.port = port;
        this.objectMapper = objectMapper;
    }

    public void connect() throws Exception {
        WebSocketClient client = new StandardWebSocketClient();

        WebSocketHandler handler = new AbstractWebSocketHandler() {
            private boolean first = true;

            @Override
            public void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
                if (first) {
                    firstMessage.complete(message.getPayload());
                    first = false;
                } else {
                    secondMessage.complete(message.getPayload());
                }
            }
        };

        URI uri = new URI("ws://localhost:" + port + "/ws/game");
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();

        session = client
                .execute(handler, headers, uri)
                .get(10, TimeUnit.SECONDS);
    }

    public void sendBet(String nick, int betNumber, BigDecimal amount) throws Exception {
        BetRequest bet = new BetRequest(nick, betNumber, amount);
        String json = objectMapper.writeValueAsString(bet);
        session.sendMessage(new TextMessage(json));
    }

    public String awaitFirstMessage() throws Exception {
        return firstMessage.get(20, TimeUnit.SECONDS);
    }

    public String awaitSecondMessage() throws Exception {
        return secondMessage.get(20, TimeUnit.SECONDS);
    }

    public void close() throws Exception {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }
}
