package com.yolo.game.demo.unit.model;

import com.yolo.game.demo.model.BetResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class BetResponseTest {

    @Test
    void shouldReturnWinningMessage() {
        BetResponse response = new BetResponse("Erik", true, 7, 7, new BigDecimal("99.00"));
        String expected = "Congratulations, Erik! You guessed correctly and won 99.00";
        assertEquals(expected, response.toMessage());
    }

    @Test
    void shouldReturnLosingMessage() {
        BetResponse response = new BetResponse("Erik", false, 5, 3, BigDecimal.ZERO);
        String expected = "Sorry, Erik! You chose 5, but the winning number was 3";
        assertEquals(expected, response.toMessage());
    }
}
