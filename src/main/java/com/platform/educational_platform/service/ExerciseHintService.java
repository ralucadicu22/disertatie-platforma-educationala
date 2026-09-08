package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.HintDTO;
import com.platform.educational_platform.model.Exercise;
import com.platform.educational_platform.model.ExerciseVersion;
import com.platform.educational_platform.model.enums.HintType;
import com.platform.educational_platform.repository.ExerciseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseHintService {

    private final ExerciseRepository exerciseRepository;
    private final ObjectMapper objectMapper;


    public List<HintDTO> getAvailableHints(Long exerciseId, int mistakes) {
        List<HintDTO> allHints = getAllHints(exerciseId);
        return allHints.stream()
                .filter(h -> h.getAfterAttempt() != null && h.getAfterAttempt() <= mistakes)
                .toList();
    }

    public List<HintDTO> getAllHints(Long exerciseId) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exercițiul nu există"));

        ExerciseVersion version = exercise.getCurrentVersion();
        if (version == null) {
            return List.of();
        }

        return parseHintsFromConfig(version.getConfig());
    }

    public List<HintDTO> parseHintsFromConfig(String configJson) {
        List<HintDTO> hints = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(configJson);
            JsonNode settings = root.path("settings");

            JsonNode hintsNode = settings.path("hints");
            if (hintsNode.isArray() && hintsNode.size() > 0) {
                for (JsonNode h : hintsNode) {
                    HintDTO dto = HintDTO.builder()
                            .text(h.path("text").asText(""))
                            .afterAttempt(h.path("afterAttempt").asInt(0))
                            .type(parseHintType(h.path("type").asText("TEXT")))
                            .targetElementId(h.path("targetElementId").asText(null))
                            .build();
                    if (!dto.getText().isEmpty()) {
                        hints.add(dto);
                    }
                }
            } else {

                JsonNode legacyHint = settings.path("hint");
                if (legacyHint.isTextual() && !legacyHint.asText().isEmpty()) {
                    hints.add(HintDTO.builder()
                            .text(legacyHint.asText())
                            .afterAttempt(2)
                            .type(HintType.TEXT)
                            .build());
                }
            }
        } catch (Exception e) {
            return List.of();
        }
        hints.sort(Comparator.comparingInt(h -> h.getAfterAttempt() != null ? h.getAfterAttempt() : 0));
        return hints;
    }

    private HintType parseHintType(String raw) {
        try {
            return HintType.valueOf(raw);
        } catch (Exception e) {
            return HintType.TEXT;
        }
    }
}