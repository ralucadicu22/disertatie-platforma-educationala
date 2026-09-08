package com.platform.educational_platform.controller;

import com.platform.educational_platform.dto.StudentFeedbackDTO;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.service.StudentFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/student-feedback")
@RequiredArgsConstructor
public class StudentFeedbackController {

    private final StudentFeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<StudentFeedbackDTO.FeedbackInfo> submit(
            @RequestBody StudentFeedbackDTO.SubmitRequest req,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(feedbackService.submitFeedback(req, currentUser));
    }

    @DeleteMapping("/my/exercise/{exerciseId}")
    public ResponseEntity<Void> deleteMy(
            @PathVariable Long exerciseId,
            @AuthenticationPrincipal User currentUser) {
        feedbackService.deleteMyFeedback(exerciseId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my/exercise/{exerciseId}")
    public ResponseEntity<StudentFeedbackDTO.FeedbackInfo> getMyForExercise(
            @PathVariable Long exerciseId,
            @AuthenticationPrincipal User currentUser) {
        Optional<StudentFeedbackDTO.FeedbackInfo> feedback =
                feedbackService.getMyFeedbackForExercise(exerciseId, currentUser);
        return feedback.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/my")
    public ResponseEntity<List<StudentFeedbackDTO.FeedbackInfo>> getMy(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(feedbackService.getMyFeedback(currentUser));
    }

    @GetMapping("/exercise/{exerciseId}")
    public ResponseEntity<List<StudentFeedbackDTO.FeedbackInfo>> getForExercise(
            @PathVariable Long exerciseId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(feedbackService.getFeedbackForExercise(exerciseId, currentUser));
    }

    @GetMapping("/exercise/{exerciseId}/summary")
    public ResponseEntity<StudentFeedbackDTO.ExerciseFeedbackSummary> getExerciseSummary(
            @PathVariable Long exerciseId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(feedbackService.getExerciseSummary(exerciseId, currentUser));
    }
}