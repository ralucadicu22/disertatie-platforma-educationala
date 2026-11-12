package com.platform.educational_platform.validation.validators;

import com.platform.educational_platform.validation.ExerciseValidator;
import com.platform.educational_platform.validation.ValidationResult;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
public class VideoValidator implements ExerciseValidator {

    private final ObjectMapper objectMapper;

    public VideoValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getSupportedType() {
        return "VIDEO";
    }

    @Override
    @SuppressWarnings("unchecked")
    public ValidationResult validate(JsonNode config, Object answer) {
        Map<String, Object> rawAnswer = objectMapper.convertValue(answer, Map.class);
        boolean watched = false;
        if (rawAnswer != null && rawAnswer.get("watched") instanceof Boolean b) {
            watched = b;
        }

        return ValidationResult.builder()
                .correct(watched)
                .score(watched ? 1.0 : 0.0)
                .details(Map.of("watched", watched))
                .build();
    }

    @Override
    public boolean isElementCorrect(JsonNode config, String elementId) {
        return false;
    }
}