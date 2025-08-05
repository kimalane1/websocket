package com.yolo.game.demo.unit.service;

import com.yolo.game.demo.model.BetRequest;
import com.yolo.game.demo.model.BetResponse;
import com.yolo.game.demo.service.GameService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {
    private final GameService gameService = new GameService(null);
    @Test
    void shouldAcceptBetWhenWaitingForBets() {
        gameService.startNewRound();
        gameService.receive(new BetRequest("Erik", 5, new BigDecimal("10")));

        assertTrue(gameService.getCurrentBets().containsKey("Erik"));
    }

    @Test
    void shouldRejectBetWhenNotAcceptingBets() {
        gameService.receive(new BetRequest("Erik", 5, new BigDecimal("10")));

        assertFalse(gameService.getCurrentBets().containsKey("Erik"));
    }

    @Test
    void shouldReturnSuccessWhenBetIsCorrect() {
        BetRequest bet = new BetRequest("Erik", 7, new BigDecimal("10"));

        BetResponse response = gameService.evaluate(7, bet);

        assertTrue(response.success());
        assertEquals(new BigDecimal("99.00"), response.amount());
        assertEquals(7, response.winningNumber());
        assertEquals("Erik", response.nick());
        assertEquals(7, response.bet());

    }

    @Test
    void shouldReturnFailureWhenBetIsIncorrect() {
        BetRequest bet = new BetRequest("Erik", 5, new BigDecimal("10"));

        BetResponse response = gameService.evaluate(3, bet);

        assertFalse(response.success());
        assertEquals(BigDecimal.ZERO, response.amount());
        assertEquals(3, response.winningNumber());
        assertEquals("Erik", response.nick());
        assertEquals(5, response.bet());
    }
}
