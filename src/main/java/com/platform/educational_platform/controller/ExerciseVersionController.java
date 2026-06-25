package com.platform.educational_platform.controller;

import com.platform.educational_platform.dto.exercise.CreateVersionRequest;
import com.platform.educational_platform.dto.exercise.ExerciseVersionDTO;
import com.platform.educational_platform.dto.exercise.ReviewActionRequest;
import com.platform.educational_platform.service.ExerciseVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseVersionController {

    private final ExerciseVersionService versionService;

    @GetMapping("/{exerciseId}/versions")
    public ResponseEntity<List<ExerciseVersionDTO>> list(@PathVariable Long exerciseId) {
        return ResponseEntity.ok(versionService.listVersions(exerciseId));
    }

    @PostMapping("/{exerciseId}/versions")
    public ResponseEntity<ExerciseVersionDTO> createDraft(
            @PathVariable Long exerciseId,
            @RequestBody CreateVersionRequest req,
            Authentication authentication) {
        return ResponseEntity.ok(versionService.createDraft(exerciseId, req, authentication.getName()));
    }


    @PostMapping("/versions/{versionId}/submit-for-review")
    public ResponseEntity<ExerciseVersionDTO> submitForReview(
            @PathVariable Long versionId,
            @RequestBody(required = false) ReviewActionRequest req) {
        return ResponseEntity.ok(versionService.submitForReview(versionId, req));
    }

    @PostMapping("/versions/{versionId}/approve")
    public ResponseEntity<ExerciseVersionDTO> approve(
            @PathVariable Long versionId,
            @RequestBody(required = false) ReviewActionRequest req,
            Authentication authentication) {
        return ResponseEntity.ok(versionService.approve(versionId, req, authentication.getName()));
    }

    @PostMapping("/versions/{versionId}/reject")
    public ResponseEntity<ExerciseVersionDTO> reject(
            @PathVariable Long versionId,
            @RequestBody ReviewActionRequest req,
            Authentication authentication) {
        return ResponseEntity.ok(versionService.reject(versionId, req, authentication.getName()));
    }

    @GetMapping("/versions/pending-review")
    public ResponseEntity<List<ExerciseVersionDTO>> listPendingReview() {
        return ResponseEntity.ok(versionService.listPendingReview());
    }

    @PostMapping("/versions/{versionId}/publish")
    public ResponseEntity<ExerciseVersionDTO> publish(@PathVariable Long versionId) {
        return ResponseEntity.ok(versionService.publish(versionId));
    }

    @PostMapping("/versions/{versionId}/archive")
    public ResponseEntity<ExerciseVersionDTO> archive(@PathVariable Long versionId) {
        return ResponseEntity.ok(versionService.archive(versionId));
    }
}