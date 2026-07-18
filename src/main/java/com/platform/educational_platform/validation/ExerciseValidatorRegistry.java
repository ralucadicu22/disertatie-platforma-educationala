package com.platform.educational_platform.validation;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ExerciseValidatorRegistry {

    private final Map<String, ExerciseValidator> validatorsByType;

    public ExerciseValidatorRegistry(List<ExerciseValidator> validators) {
        this.validatorsByType = validators.stream()
                .collect(Collectors.toMap(ExerciseValidator::getSupportedType, v -> v));
    }

    public ExerciseValidator getValidator(String type) {
        ExerciseValidator validator = validatorsByType.get(type);
        if (validator == null) {
            throw new RuntimeException("Nu exista validator implementat pentru tipul: " + type);
        }
        return validator;
    }
}