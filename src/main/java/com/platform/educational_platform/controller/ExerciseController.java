package com.platform.educational_platform.controller;

import com.platform.educational_platform.dto.exercise.ExerciseDTO;
import com.platform.educational_platform.dto.exercise.submission.*;
import com.platform.educational_platform.dto.exercise.version.ExerciseVersionDTO;
import com.platform.educational_platform.dto.exercise.version.SaveDraftRequest;
import com.platform.educational_platform.dto.metadata.UpdateExerciseMetadataRequest;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.model.enums.GradeLevel;
import com.platform.educational_platform.model.enums.VersionStatus;
import com.platform.educational_platform.service.ExerciseService;
import com.platform.educational_platform.service.ExerciseSubmissionService;
import com.platform.educational_platform.service.ExerciseVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ExerciseController {

    private final ExerciseService exerciseService;
    private final ExerciseVersionService versionService;
    private final ExerciseSubmissionService submissionService;

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<List<ExerciseDTO>> getExercisesByQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(exerciseService.getExercisesByQuiz(quizId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExerciseDTO> getExerciseById(@PathVariable Long id) {
        return ResponseEntity.ok(exerciseService.getExerciseById(id));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ExerciseDTO>> filter(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) GradeLevel gradeLevel,
            @RequestParam(required = false) Long tagId,
            @RequestParam(required = false) VersionStatus status) {
        return ResponseEntity.ok(
                exerciseService.filter(type, difficulty, topic, gradeLevel, tagId, status));
    }

    @PutMapping("/{exerciseId}/metadata")
    public ResponseEntity<ExerciseDTO> updateMetadata(
            @PathVariable Long exerciseId,
            @RequestBody UpdateExerciseMetadataRequest req) {
        return ResponseEntity.ok(exerciseService.updateMetadata(exerciseId, req));
    }

    @PostMapping("/draft/new")
    public ResponseEntity<ExerciseVersionDTO> createNewExerciseWithDraft(
            @RequestParam Long quizId,
            @RequestBody SaveDraftRequest req,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(versionService.createNewExerciseWithDraft(
                quizId, req, currentUser.getEmail()));
    }

    @PutMapping("/{exerciseId}/draft")
    public ResponseEntity<ExerciseVersionDTO> upsertDraft(
            @PathVariable Long exerciseId,
            @RequestBody SaveDraftRequest req,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(versionService.upsertDraft(
                exerciseId, req, currentUser.getEmail()));
    }

    @GetMapping("/{exerciseId}/draft")
    public ResponseEntity<ExerciseVersionDTO> getCurrentDraft(
            @PathVariable Long exerciseId) {
        return ResponseEntity.ok(versionService.getCurrentDraft(exerciseId));
    }

    @PostMapping("/submit")
    public ResponseEntity<SubmitAnswerResponse> submit(
            @RequestBody SubmitAnswerRequest req,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(submissionService.submit(req, currentUser.getEmail()));
    }

    @PostMapping("/check-element")
    public ResponseEntity<CheckElementResponse> checkElement(@RequestBody CheckElementRequest req) {
        return ResponseEntity.ok(submissionService.checkElement(req));
    }

    @PostMapping("/check-placement")
    public ResponseEntity<CheckPlacementResponse> checkPlacement(@RequestBody CheckPlacementRequest req) {
        return ResponseEntity.ok(submissionService.checkPlacement(req));
    }
}