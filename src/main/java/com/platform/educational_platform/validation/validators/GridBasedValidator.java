package com.platform.educational_platform.validation.validators;

import com.platform.educational_platform.validation.ExerciseValidator;
import com.platform.educational_platform.validation.ValidationResult;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class GridBasedValidator implements ExerciseValidator {

    private final ObjectMapper objectMapper;

    public GridBasedValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getSupportedType() {
        return "GRID_BASED";
    }

    @Override
    public ValidationResult validate(JsonNode config, Object answer) {
        Set<String> correctCells = new HashSet<>();
        config.path("solution").path("correctCells")
                .forEach(node -> correctCells.add(node.asString()));

        List<String> submittedList = objectMapper.convertValue(
                answer,
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        Set<String> submittedCells = new HashSet<>(submittedList != null ? submittedList : List.of());

        Set<String> correctlySelected = new HashSet<>(correctCells);
        correctlySelected.retainAll(submittedCells);

        Set<String> incorrectlySelected = new HashSet<>(submittedCells);
        incorrectlySelected.removeAll(correctCells);

        Set<String> missed = new HashSet<>(correctCells);
        missed.removeAll(submittedCells);

        boolean foundAll = correctlySelected.size() == correctCells.size();
        boolean noMistakes = incorrectlySelected.isEmpty();

        String scoringMode = config.path("settings").path("scoringMode").asString("integral");

        double score;
        boolean isCorrect;

        if ("partial".equals(scoringMode)) {
            isCorrect = foundAll && noMistakes;
            score = correctCells.isEmpty() ? 0.0
                    : Math.max(0.0, ((double) correctlySelected.size() - incorrectlySelected.size()) / correctCells.size());
        } else {
            isCorrect = foundAll && noMistakes;
            score = isCorrect ? 1.0 : 0.0;
        }

        return ValidationResult.builder()
                .correct(isCorrect)
                .score(score)
                .details(Map.of(
                        "correctCells", new ArrayList<>(correctCells),
                        "correctlySelected", new ArrayList<>(correctlySelected),
                        "incorrectlySelected", new ArrayList<>(incorrectlySelected),
                        "missed", new ArrayList<>(missed)
                ))
                .build();
    }

    @Override
    public boolean isElementCorrect(JsonNode config, String elementId) {
        for (JsonNode node : config.path("solution").path("correctCells")) {
            if (node.asString().equals(elementId)) return true;
        }
        return false;
    }
}