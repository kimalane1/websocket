package com.yolo.game.demo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolo.game.demo.config.WebSocketTestClient;
import com.yolo.game.demo.service.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GameService gameService;

    @Test
    void shouldReceiveMessageAboutWinFromServer() throws Exception {
        WebSocketTestClient client = new WebSocketTestClient(port, objectMapper);
        client.connect();

        client.sendBet("Erik", 5, new BigDecimal("10.00"));
        gameService.startNewRound();

        String response = client.awaitFirstMessage();
        assertEquals("\"Bet accepted from Erik\"", response);

        gameService.finishRound(5);
        String calcResponse = client.awaitSecondMessage();
        assertEquals("\"Congratulations, Erik! You guessed correctly and won 99.00\"", calcResponse);

        client.close();
    }

    @Test
    void shouldReceiveSorryMessageFromServer() throws Exception {
        WebSocketTestClient client = new WebSocketTestClient(port, objectMapper);
        client.connect();

        client.sendBet("Erik", 3, new BigDecimal("10.00"));
        gameService.startNewRound();

        String response = client.awaitFirstMessage();
        assertEquals("\"Bet accepted from Erik\"", response);

        gameService.finishRound(5);
        String calcResponse = client.awaitSecondMessage();
        assertEquals("\"Sorry, Erik! You chose 3, but the winning number was 5\"", calcResponse);

        client.close();
    }
}

