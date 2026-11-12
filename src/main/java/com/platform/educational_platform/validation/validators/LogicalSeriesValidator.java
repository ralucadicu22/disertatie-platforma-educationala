package com.platform.educational_platform.validation.validators;

import com.platform.educational_platform.validation.ExerciseValidator;
import com.platform.educational_platform.validation.ValidationResult;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Component
public class LogicalSeriesValidator implements ExerciseValidator {

    private final ObjectMapper objectMapper;

    public LogicalSeriesValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getSupportedType() {
        return "LOGICAL_SERIES";
    }

    @Override
    @SuppressWarnings("unchecked")
    public ValidationResult validate(JsonNode config, Object answer) {
        JsonNode correctNode = config.path("solution").path("correctAnswers");
        Map<String, String> correctAnswers = new HashMap<>();
        correctNode.properties().forEach(entry ->
                correctAnswers.put(entry.getKey(), entry.getValue().asString("").trim().toLowerCase()));

        Map<String, Object> rawAnswer = objectMapper.convertValue(answer, Map.class);
        Map<String, String> submittedAnswers = new HashMap<>();
        if (rawAnswer != null) {
            rawAnswer.forEach((k, v) -> submittedAnswers.put(k, v != null ? v.toString().trim().toLowerCase() : ""));
        }

        String scoringMode = config.path("settings").path("scoringMode").asString("integral");
        int total = correctAnswers.size();
        int correct = 0;
        Map<String, Boolean> perZone = new HashMap<>();

        for (Map.Entry<String, String> entry : correctAnswers.entrySet()) {
            String zoneId = entry.getKey();
            String expected = entry.getValue();
            String submitted = submittedAnswers.getOrDefault(zoneId, "");
            boolean ok = submitted.equals(expected);
            if (ok) correct++;
            perZone.put(zoneId, ok);
        }

        double score;
        boolean isCorrect;

        if ("partial".equals(scoringMode)) {
            isCorrect = correct == total;
            score = total == 0 ? 0.0 : (double) correct / total;
        } else {
            isCorrect = correct == total;
            score = isCorrect ? 1.0 : 0.0;
        }

        return ValidationResult.builder()
                .correct(isCorrect)
                .score(score)
                .details(Map.of("perZone", perZone, "correctAnswers", correctAnswers))
                .build();
    }

    @Override
    public boolean isElementCorrect(JsonNode config, String elementId) {
        return false;
    }
}