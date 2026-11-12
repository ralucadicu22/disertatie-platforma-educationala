package com.platform.educational_platform.validation.validators;

import com.platform.educational_platform.validation.ExerciseValidator;
import com.platform.educational_platform.validation.ValidationResult;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
public class PdfValidator implements ExerciseValidator {

    private final ObjectMapper objectMapper;

    public PdfValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getSupportedType() {
        return "PDF";
    }

    @Override
    @SuppressWarnings("unchecked")
    public ValidationResult validate(JsonNode config, Object answer) {
        boolean requiresOpen = config.path("settings").path("requiresOpen").asBoolean(true);

        Map<String, Object> rawAnswer = objectMapper.convertValue(answer, Map.class);
        boolean opened = false;
        if (rawAnswer != null && rawAnswer.get("opened") instanceof Boolean b) {
            opened = b;
        }

        boolean isCorrect = !requiresOpen || opened;

        return ValidationResult.builder()
                .correct(isCorrect)
                .score(isCorrect ? 1.0 : 0.0)
                .details(Map.of("opened", opened, "requiresOpen", requiresOpen))
                .build();
    }

    @Override
    public boolean isElementCorrect(JsonNode config, String elementId) {
        return false;
    }
}