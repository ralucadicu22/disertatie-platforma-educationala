package com.platform.educational_platform.validation.validators;

import com.platform.educational_platform.validation.ExerciseValidator;
import com.platform.educational_platform.validation.ValidationResult;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Component
public class FindAndCountValidator implements ExerciseValidator {

    private final ObjectMapper objectMapper;

    public FindAndCountValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getSupportedType() {
        return "FIND_AND_COUNT";
    }

    @Override
    @SuppressWarnings("unchecked")
    public ValidationResult validate(JsonNode config, Object answer) {
        JsonNode correctCountsNode = config.path("solution").path("correctCounts");
        Map<String, Integer> correctCounts = new HashMap<>();
        correctCountsNode.properties().forEach(entry ->
                correctCounts.put(entry.getKey(), entry.getValue().asInt()));

        Map<String, Object> rawAnswer = objectMapper.convertValue(answer, Map.class);
        Map<String, Integer> submittedCounts = new HashMap<>();
        if (rawAnswer != null) {
            rawAnswer.forEach((k, v) -> {
                if (v instanceof Number num) {
                    submittedCounts.put(k, num.intValue());
                }
            });
        }

        String scoringMode = config.path("settings").path("scoringMode").asString("integral");

        int total = correctCounts.size();
        int correct = 0;
        Map<String, Object> details = new HashMap<>();
        Map<String, Integer> correctCountsCopy = new HashMap<>(correctCounts);
        details.put("correctCounts", correctCountsCopy);
        Map<String, Boolean> perCategory = new HashMap<>();

        for (Map.Entry<String, Integer> entry : correctCounts.entrySet()) {
            String catId = entry.getKey();
            int expected = entry.getValue();
            int submitted = submittedCounts.getOrDefault(catId, 0);
            boolean ok = submitted == expected;
            if (ok) correct++;
            perCategory.put(catId, ok);
        }
        details.put("perCategory", perCategory);

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
                .details(details)
                .build();
    }

    @Override
    public boolean isElementCorrect(JsonNode config, String elementId) {
        return false;
    }
}