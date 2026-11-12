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
public class OrderingValidator implements ExerciseValidator {

    private final ObjectMapper objectMapper;

    public OrderingValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getSupportedType() {
        return "ORDERING";
    }

    @Override
    public ValidationResult validate(JsonNode config, Object answer) {
        List<String> correctOrder = new ArrayList<>();
        config.path("solution").path("correctElementIds")
                .forEach(node -> correctOrder.add(node.asString()));

        List<String> studentOrder = objectMapper.convertValue(
                answer,
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        if (studentOrder == null) studentOrder = List.of();

        boolean correct = correctOrder.equals(studentOrder);

        int matchingPositions = 0;
        int total = correctOrder.size();
        for (int i = 0; i < Math.min(total, studentOrder.size()); i++) {
            if (correctOrder.get(i).equals(studentOrder.get(i))) {
                matchingPositions++;
            }
        }

        double score = total == 0 ? 0.0 : (double) matchingPositions / total;

        return ValidationResult.builder()
                .correct(correct)
                .score(score)
                .details(Map.of(
                        "correctOrder", correctOrder,
                        "studentOrder", studentOrder,
                        "matchingPositions", matchingPositions
                ))
                .build();
    }
}