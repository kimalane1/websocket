package com.yolo.game.demo.model;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record BetRequest(@NotBlank(message = "Nick must not be blank") String nick,
                         @Min(value = 1, message = "Bet must be between 1 and 10")
                         @Max(value = 10, message = "Bet must be between 1 and 10") int bet,
                         @NotNull(message = "Amount is required") @Digits(integer = 10, fraction = 2, message = "Amount must have at most 2 decimal places") @DecimalMin(value = "0.01", message = "Amount must be at least 0.01") BigDecimal amount) {
}
