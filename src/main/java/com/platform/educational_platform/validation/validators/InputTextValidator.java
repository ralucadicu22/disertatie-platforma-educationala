package com.platform.educational_platform.validation.validators;

import com.platform.educational_platform.validation.ExerciseValidator;
import com.platform.educational_platform.validation.ValidationResult;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InputTextValidator implements ExerciseValidator {

    private final ObjectMapper objectMapper;

    public InputTextValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getSupportedType() {
        return "INPUT_TEXT";
    }

    @Override
    public ValidationResult validate(JsonNode config, Object answer) {
        Map<String, String> correctAnswersRaw = objectMapper.convertValue(
                config.path("solution").path("correctAnswers"),
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
        if (correctAnswersRaw == null) correctAnswersRaw = Map.of();

        Map<String, List<String>> acceptedByElement = new HashMap<>();
        for (Map.Entry<String, String> entry : correctAnswersRaw.entrySet()) {
            List<String> accepted = new ArrayList<>();
            accepted.add(entry.getValue().trim().toLowerCase());
            acceptedByElement.put(entry.getKey(), accepted);
        }

        JsonNode alternativeAnswers = config.path("solution").path("alternativeAnswers");
        if (alternativeAnswers.isArray()) {
            for (JsonNode alt : alternativeAnswers) {
                String elementId = alt.path("elementId").asString(null);
                String value = alt.path("value").asString(null);
                if (elementId != null && value != null) {
                    acceptedByElement.computeIfAbsent(elementId, k -> new ArrayList<>())
                            .add(value.trim().toLowerCase());
                }
            }
        }

        Map<String, String> studentAnswers = objectMapper.convertValue(
                answer,
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
        if (studentAnswers == null) studentAnswers = Map.of();

        List<String> correctIds = new ArrayList<>();
        List<String> incorrectIds = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : acceptedByElement.entrySet()) {
            String elementId = entry.getKey();
            String given = studentAnswers.getOrDefault(elementId, "").trim().toLowerCase();
            if (entry.getValue().contains(given)) {
                correctIds.add(elementId);
            } else {
                incorrectIds.add(elementId);
            }
        }

        int total = acceptedByElement.size();
        boolean correct = incorrectIds.isEmpty() && total > 0;
        double score = total == 0 ? 0.0 : (double) correctIds.size() / total;

        return ValidationResult.builder()
                .correct(correct)
                .score(score)
                .details(Map.of(
                        "correctElementIds", correctIds,
                        "incorrectElementIds", incorrectIds
                ))
                .build();
    }
}