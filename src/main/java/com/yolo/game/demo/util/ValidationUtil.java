package com.yolo.game.demo.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ValidationUtil {
    private final Validator validator;

    public ValidationUtil(Validator validator) {
        this.validator = validator;
    }

    public <T> Optional<String> getValidationErrorMessage(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (violations.isEmpty()) return Optional.empty();

        return Optional.of(violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; ")));
    }
}
