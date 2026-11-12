package com.platform.educational_platform.validation.validators;

import com.platform.educational_platform.validation.ExerciseValidator;
import com.platform.educational_platform.validation.ValidationResult;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Component
public class PuzzleValidator implements ExerciseValidator {

    private final ObjectMapper objectMapper;

    public PuzzleValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getSupportedType() {
        return "PUZZLE";
    }

    @Override
    @SuppressWarnings("unchecked")
    public ValidationResult validate(JsonNode config, Object answer) {
        JsonNode correctNode = config.path("solution").path("correctPlacements");
        Map<String, String> correctPlacements = new HashMap<>();
        correctNode.properties().forEach(entry ->
                correctPlacements.put(entry.getKey(), entry.getValue().asString("")));

        Map<String, Object> rawAnswer = objectMapper.convertValue(answer, Map.class);
        Map<String, String> submittedPlacements = new HashMap<>();
        if (rawAnswer != null) {
            rawAnswer.forEach((k, v) -> submittedPlacements.put(k, v != null ? v.toString() : ""));
        }

        String scoringMode = config.path("settings").path("scoringMode").asString("integral");
        int total = correctPlacements.size();
        int correct = 0;
        Map<String, Boolean> perPiece = new HashMap<>();

        for (Map.Entry<String, String> entry : correctPlacements.entrySet()) {
            String pieceId = entry.getKey();
            String expectedZone = entry.getValue();
            String submittedZone = submittedPlacements.getOrDefault(pieceId, "");
            boolean ok = expectedZone.equals(submittedZone);
            if (ok) correct++;
            perPiece.put(pieceId, ok);
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
                .details(Map.of("perPiece", perPiece, "correctPlacements", correctPlacements))
                .build();
    }

    @Override
    public boolean isElementCorrect(JsonNode config, String elementId) {
        return false;
    }
}