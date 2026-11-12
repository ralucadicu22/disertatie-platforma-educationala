package com.platform.educational_platform.validation.validators;

import com.platform.educational_platform.validation.ExerciseValidator;
import com.platform.educational_platform.validation.ValidationResult;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class InputNumericValidator implements ExerciseValidator {

    private final ObjectMapper objectMapper;

    public InputNumericValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getSupportedType() {
        return "INPUT_NUMERIC";
    }

    @Override
    public ValidationResult validate(JsonNode config, Object answer) {
        Map<String, String> correctAnswersRaw = objectMapper.convertValue(
                config.path("solution").path("correctAnswers"),
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
        if (correctAnswersRaw == null) correctAnswersRaw = Map.of();

        double tolerance = config.path("settings").path("tolerance").asDouble(0.0);

        Map<String, String> studentAnswers = objectMapper.convertValue(
                answer,
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
        if (studentAnswers == null) studentAnswers = Map.of();

        List<String> correctIds = new ArrayList<>();
        List<String> incorrectIds = new ArrayList<>();

        for (Map.Entry<String, String> entry : correctAnswersRaw.entrySet()) {
            String elementId = entry.getKey();
            double expected;
            try {
                expected = Double.parseDouble(entry.getValue());
            } catch (NumberFormatException e) {
                incorrectIds.add(elementId);
                continue;
            }
            double given;
            try {
                given = Double.parseDouble(studentAnswers.getOrDefault(elementId, "").trim());
            } catch (NumberFormatException e) {
                incorrectIds.add(elementId);
                continue;
            }
            if (Math.abs(given - expected) <= tolerance) {
                correctIds.add(elementId);
            } else {
                incorrectIds.add(elementId);
            }
        }

        int total = correctIds.size() + incorrectIds.size();
        boolean correct = incorrectIds.isEmpty() && total > 0;
        double score = total == 0 ? 0.0 : (double) correctIds.size() / total;

        return ValidationResult.builder()
                .correct(correct)
                .score(score)
                .details(Map.of(
                        "correctElementIds", correctIds,
                        "incorrectElementIds", incorrectIds,
                        "tolerance", tolerance
                ))
                .build();
    }
}