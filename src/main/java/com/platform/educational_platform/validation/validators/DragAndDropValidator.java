package com.platform.educational_platform.validation.validators;

import com.platform.educational_platform.validation.ExerciseValidator;
import com.platform.educational_platform.validation.ValidationResult;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Component
public class DragAndDropValidator implements ExerciseValidator {

    private final ObjectMapper objectMapper;

    public DragAndDropValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String getSupportedType() {
        return "DRAG_AND_DROP";
    }

    private Map<String, String> extractDropMapping(JsonNode config) {
        Map<String, String> mapping = objectMapper.convertValue(
                config.path("solution").path("dropMapping"),
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
        return mapping != null ? mapping : Map.of();
    }

    @Override
    public ValidationResult validate(JsonNode config, Object answer) {
        Map<String, String> correctMapping = extractDropMapping(config);

        Map<String, String> submittedMapping = objectMapper.convertValue(
                answer,
                objectMapper.getTypeFactory().constructMapType(Map.class, String.class, String.class));
        if (submittedMapping == null) submittedMapping = Map.of();

        int correctPlacements = 0;
        for (Map.Entry<String, String> entry : correctMapping.entrySet()) {
            if (entry.getValue().equals(submittedMapping.get(entry.getKey()))) {
                correctPlacements++;
            }
        }

        String scoringMode = config.path("settings").path("scoringMode").asString("integral");
        boolean allCorrect = correctPlacements == correctMapping.size();
        boolean noExtra = submittedMapping.size() <= correctMapping.size();
        boolean correct = "partial".equals(scoringMode) ? allCorrect : (allCorrect && noExtra);

        double score = correctMapping.isEmpty()
                ? 0.0
                : (double) correctPlacements / correctMapping.size();

        return ValidationResult.builder()
                .correct(correct)
                .score(score)
                .details(Map.of(
                        "correctMapping", correctMapping,
                        "submittedMapping", submittedMapping,
                        "correctPlacements", correctPlacements
                ))
                .build();
    }

    @Override
    public boolean isPlacementCorrect(JsonNode config, String draggableId, String targetId) {
        Map<String, String> mapping = extractDropMapping(config);
        return targetId.equals(mapping.get(draggableId));
    }
}