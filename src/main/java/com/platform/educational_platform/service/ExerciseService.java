package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.exercise.ExerciseDTO;
import com.platform.educational_platform.dto.metadata.TagDTO;
import com.platform.educational_platform.dto.metadata.UpdateExerciseMetadataRequest;
import com.platform.educational_platform.model.Exercise;
import com.platform.educational_platform.model.ExerciseVersion;
import com.platform.educational_platform.model.enums.GradeLevel;
import com.platform.educational_platform.model.enums.VersionStatus;
import com.platform.educational_platform.repository.ExerciseRepository;
import com.platform.educational_platform.specification.ExerciseSpecifications;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final TagService tagService;
    private final ObjectMapper objectMapper;

    public List<ExerciseDTO> getExercisesByQuiz(Long quizId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isEditorOrAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("EDITOR") || a.getAuthority().equals("ADMIN"));

        return exerciseRepository.findByQuizIdOrderByOrderIndexAsc(quizId)
                .stream()
                .filter(e -> isEditorOrAdmin || (e.getCurrentVersion() != null
                        && e.getCurrentVersion().getStatus() == VersionStatus.PUBLISHED))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ExerciseDTO getExerciseById(Long id) {
        Exercise e = exerciseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exercitiul nu a fost gasit"));
        return toDTO(e);
    }

    public List<ExerciseDTO> filter(String type, String difficulty, String topic,
                                    GradeLevel gradeLevel, Long tagId, VersionStatus status) {
        Specification<Exercise> spec = Specification.allOf(
                ExerciseSpecifications.hasType(type),
                ExerciseSpecifications.hasDifficulty(difficulty),
                ExerciseSpecifications.hasTopic(topic),
                ExerciseSpecifications.hasGradeLevel(gradeLevel),
                ExerciseSpecifications.hasTagId(tagId),
                ExerciseSpecifications.hasCurrentVersionStatus(status)
        );

        return exerciseRepository.findAll(spec)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExerciseDTO updateMetadata(Long exerciseId, UpdateExerciseMetadataRequest req) {
        Exercise exercise = exerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new RuntimeException("Exercitiul nu exista"));

        if (req.getTopic() != null) exercise.setTopic(req.getTopic());
        if (req.getGradeLevel() != null) exercise.setGradeLevel(req.getGradeLevel());
        if (req.getDifficulty() != null) exercise.setDifficulty(req.getDifficulty());
        if (req.getTagIds() != null) {
            exercise.setTags(tagService.resolveTagIds(req.getTagIds()));
        }

        return toDTO(exerciseRepository.save(exercise));
    }

    private String sanitizeConfigForRole(String configJson, boolean isEditorOrAdmin) {
        if (configJson == null || isEditorOrAdmin) {
            return configJson;
        }
        try {
            ObjectNode root = (ObjectNode) objectMapper.readTree(configJson);
            JsonNode solutionNode = root.path("solution");
            int correctCount = solutionNode.path("correctElementIds").size();

            ObjectNode safeSolution = objectMapper.createObjectNode();
            safeSolution.put("correctCount", correctCount);
            root.set("solution", safeSolution);

            return objectMapper.writeValueAsString(root);
        } catch (Exception e) {
            return null;
        }
    }

    private ExerciseDTO toDTO(Exercise e) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isEditorOrAdmin = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("EDITOR") || a.getAuthority().equals("ADMIN"));

        ExerciseVersion active = e.getCurrentVersion();
        String config = null;
        if (active != null && active.getStatus() == VersionStatus.PUBLISHED) {
            config = sanitizeConfigForRole(active.getConfig(), isEditorOrAdmin);
        }

        Set<TagDTO> tagDTOs = e.getTags() == null ? null : e.getTags().stream()
                .map(t -> TagDTO.builder()
                        .id(t.getId())
                        .name(t.getName())
                        .description(t.getDescription())
                        .build())
                .collect(Collectors.toSet());

        return ExerciseDTO.builder()
                .id(e.getId())
                .type(e.getType())
                .config(config)
                .points(e.getPoints())
                .difficulty(e.getDifficulty())
                .orderIndex(e.getOrderIndex())
                .maxAttempts(e.getMaxAttempts())
                .topic(e.getTopic())
                .gradeLevel(e.getGradeLevel())
                .tags(tagDTOs)
                .build();
    }
}