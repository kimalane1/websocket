package com.yolo.game.demo.model;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record BetRequest(@NotBlank(message = "Nick must not be blank") String nick,
                         @NotNull(message = "Bet is required")
                         @Min(value = 1, message = "Bet must be between 1 and 10")
                         @Max(value = 10, message = "Bet must be between 1 and 10") int bet,
                         @NotNull(message = "Amount is required") @DecimalMin(value = "0.01", message = "Amount must be at least 0.01") BigDecimal amount) {
}
