package com.platform.educational_platform.validation.validators;

import com.platform.educational_platform.validation.ExerciseValidator;
import com.platform.educational_platform.validation.ValidationResult;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Component
public class TextProblemsValidator implements ExerciseValidator {

    private final ObjectMapper objectMapper;

    public TextProblemsValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getSupportedType() {
        return "TEXT_PROBLEMS";
    }

    @Override
    @SuppressWarnings("unchecked")
    public ValidationResult validate(JsonNode config, Object answer) {
        JsonNode correctNode = config.path("solution").path("correctAnswers");
        Map<String, String> correctAnswers = new HashMap<>();
        correctNode.properties().forEach(entry ->
                correctAnswers.put(entry.getKey(), normalize(entry.getValue().asString(""))));

        Map<String, Object> rawAnswer = objectMapper.convertValue(answer, Map.class);
        Map<String, String> submittedAnswers = new HashMap<>();
        if (rawAnswer != null) {
            rawAnswer.forEach((k, v) -> submittedAnswers.put(k, normalize(v != null ? v.toString() : "")));
        }

        double tolerance = config.path("settings").path("tolerance").asDouble(0.0);
        String scoringMode = config.path("settings").path("scoringMode").asString("integral");

        int total = correctAnswers.size();
        int correct = 0;
        Map<String, Boolean> perZone = new HashMap<>();

        for (Map.Entry<String, String> entry : correctAnswers.entrySet()) {
            String zoneId = entry.getKey();
            String expected = entry.getValue();
            String submitted = submittedAnswers.getOrDefault(zoneId, "");
            boolean ok = isMatch(expected, submitted, tolerance);
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

    private String normalize(String v) {
        return v.replace(",", ".").trim().toLowerCase();
    }

    private boolean isMatch(String expected, String submitted, double tolerance) {
        if (expected.equals(submitted)) return true;
        if (tolerance > 0) {
            try {
                BigDecimal exp = new BigDecimal(expected);
                BigDecimal sub = new BigDecimal(submitted);
                BigDecimal diff = exp.subtract(sub).abs();
                BigDecimal tol = exp.abs().multiply(BigDecimal.valueOf(tolerance)).setScale(10, RoundingMode.HALF_UP);
                return diff.compareTo(tol) <= 0;
            } catch (NumberFormatException ignored) {
            }
        }
        return false;
    }

    @Override
    public boolean isElementCorrect(JsonNode config, String elementId) {
        return false;
    }
}