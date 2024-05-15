package dev.rgonzalez.demo.todo.grpc.aspect;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class ValidatorService {

    private static ValidatorService instance;
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    private static final String COLLECTION_PATH_REGEX = "(\\[)([0-9])(\\])";

    private ValidatorService() {
    }

    public static ValidatorService getInstance() {
        if (Objects.isNull(instance)) {
            instance = new ValidatorService();
        }

        return instance;
    }

    public void validate(Object object) {
        var violations = validator.validate(object, Default.class);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(buildViolationMessage(violations));
        }
    }

    private String buildViolationMessage(Set<ConstraintViolation<Object>> constraintViolations) {
        var messages = new ArrayList<String>();

        constraintViolations.forEach(violation -> {
            if (isCollectionViolation(violation)) {
                messages.add(formatMessageForCollection(violation));
            } else {
                messages.add(violation.getMessage());
            }
        });
        Collections.sort(messages);
        return String.join(", ", messages);
    }

    private boolean isCollectionViolation(ConstraintViolation<Object> constraintViolation) {
        return Pattern.compile(COLLECTION_PATH_REGEX).matcher(constraintViolation.getPropertyPath().toString()).find();
    }

    private String formatMessageForCollection(ConstraintViolation<Object> constraintViolation) {
        return "%s %s".formatted(constraintViolation.getMessage(), constraintViolation.getInvalidValue());
    }

}