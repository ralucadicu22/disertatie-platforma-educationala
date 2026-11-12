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
public class MatchingValidator implements ExerciseValidator {

    private final ObjectMapper objectMapper;

    public MatchingValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getSupportedType() {
        return "matching";
    }

    @Override
    public ValidationResult validate(JsonNode config, Object answer) {
        Map<String, String> correctPairs = objectMapper.convertValue(
                config.path("solution").path("matchMapping"),
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));

        Map<String, String> studentPairs = objectMapper.convertValue(
                answer,
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));

        if (correctPairs == null || correctPairs.isEmpty()) {
            return ValidationResult.builder()
                    .correct(false)
                    .score(0.0)
                    .details(Map.of("correctPairIds", List.of(), "incorrectPairIds", List.of()))
                    .build();
        }

        if (studentPairs == null) {
            studentPairs = Map.of();
        }

        List<String> correctIds = new ArrayList<>();
        List<String> incorrectIds = new ArrayList<>();

        for (Map.Entry<String, String> entry : correctPairs.entrySet()) {
            String leftId = entry.getKey();
            String expectedRight = entry.getValue();
            String givenRight = studentPairs.get(leftId);
            if (expectedRight != null && expectedRight.equals(givenRight)) {
                correctIds.add(leftId);
            } else {
                incorrectIds.add(leftId);
            }
        }

        int total = correctPairs.size();
        boolean correct = incorrectIds.isEmpty() && total > 0;
        double score = total == 0 ? 0.0 : (double) correctIds.size() / total;

        return ValidationResult.builder()
                .correct(correct)
                .score(score)
                .details(Map.of("correctPairIds", correctIds, "incorrectPairIds", incorrectIds))
                .build();
    }
}