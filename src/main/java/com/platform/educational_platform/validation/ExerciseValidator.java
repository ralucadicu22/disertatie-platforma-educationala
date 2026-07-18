package com.platform.educational_platform.validation;

import tools.jackson.databind.JsonNode;

public interface ExerciseValidator {

    String getSupportedType();

    ValidationResult validate(JsonNode config, Object answer);

    default boolean isElementCorrect(JsonNode config, String elementId) {
        throw new UnsupportedOperationException(
                "Acest tip de exercitiu nu suporta verificare per element");
    }
}