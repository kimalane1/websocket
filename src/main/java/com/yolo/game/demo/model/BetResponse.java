package com.yolo.game.demo.model;

import java.math.BigDecimal;

public record BetResponse(String nick, boolean success, int bet, int winningNumber, BigDecimal amount) {
    public String toMessage() {
        return success ?
                String.format("Congratulations, %s! You guessed correctly and won %s", nick, amount) :
                String.format("Sorry, %s! You chose %d, but the winning number was %d", nick, bet, winningNumber);
    }
}
