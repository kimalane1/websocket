package com.yolo.game.demo.unit.util;

import com.yolo.game.demo.model.BetRequest;
import com.yolo.game.demo.util.ValidationUtil;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class ValidationUtilTest {
    private ValidationUtil validationUtil;
    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        validationUtil = new ValidationUtil(validator);
    }

    @Test
    void shouldReturnNoViolationsForValidBetRequest() {
        BetRequest validBet = new BetRequest("Erik", 5, new BigDecimal("10.00"));

        Optional<String> violations = validationUtil.getValidationErrorMessage(validBet);

        assertTrue(violations.isEmpty(), "Expected no validation violations");
    }

    @Test
    void shouldReturnViolationForBlankNick() {
        BetRequest invalid = new BetRequest("", 5, new BigDecimal("10.00"));

        Optional<String> violations = validationUtil.getValidationErrorMessage(invalid);

        assertTrue(violations.isPresent());
        assertTrue(violations.get().contains("Nick must not be blank"));
    }

    @Test
    void shouldReturnViolationForOutOfRangeBet() {
        BetRequest invalid = new BetRequest("Erik", 15, new BigDecimal("10.00"));

        Optional<String> violations = validationUtil.getValidationErrorMessage(invalid);

        assertTrue(violations.isPresent());
        assertTrue(violations.get().contains("Bet must be between 1 and 10"));
    }

    @Test
    void shouldReturnViolationForTooSmallAmount() {
        BetRequest invalid = new BetRequest("Erik", 5, new BigDecimal("0.00"));

        Optional<String> violations = validationUtil.getValidationErrorMessage(invalid);

        assertTrue(violations.isPresent());
        assertTrue(violations.get().contains("Amount must be at least 0.01"));
    }
}
