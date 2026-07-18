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
public class FindAndSelectValidator implements ExerciseValidator {

    private final ObjectMapper objectMapper;

    public FindAndSelectValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getSupportedType() {
        return "FIND_AND_SELECT";
    }

    @Override
    public ValidationResult validate(JsonNode config, Object answer) {
        Set<String> correctIds = new HashSet<>();
        config.path("solution").path("correctElementIds")
                .forEach(node -> correctIds.add(node.asString()));

        List<String> selectedList = objectMapper.convertValue(
                answer,
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        Set<String> selectedIds = new HashSet<>(selectedList != null ? selectedList : List.of());

        Set<String> correctlySelected = new HashSet<>(correctIds);
        correctlySelected.retainAll(selectedIds);

        Set<String> incorrectlySelected = new HashSet<>(selectedIds);
        incorrectlySelected.removeAll(correctIds);

        boolean foundAll = correctlySelected.size() == correctIds.size();
        boolean noMistakes = incorrectlySelected.isEmpty();

        String scoringMode = config.path("settings").path("scoringMode").asString("integral");


        boolean correct = "partial".equals(scoringMode) ? foundAll : (foundAll && noMistakes);

        double score = correctIds.isEmpty()
                ? 0.0
                : (double) correctlySelected.size() / correctIds.size();

        return ValidationResult.builder()
                .correct(correct)
                .score(score)
                .details(Map.of(
                        "correctElementIds", new ArrayList<>(correctIds),
                        "correctlySelectedIds", new ArrayList<>(correctlySelected),
                        "incorrectlySelectedIds", new ArrayList<>(incorrectlySelected)
                ))
                .build();
    }

    @Override
    public boolean isElementCorrect(JsonNode config, String elementId) {
        for (JsonNode node : config.path("solution").path("correctElementIds")) {
            if (node.asString().equals(elementId)) return true;
        }
        return false;
    }
}