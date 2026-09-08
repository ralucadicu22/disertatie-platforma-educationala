package com.platform.educational_platform.controller;

import com.platform.educational_platform.dto.TeacherFeedbackDTO;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.service.TeacherFeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teacher-feedback")
@RequiredArgsConstructor
public class TeacherFeedbackController {

    private final TeacherFeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<TeacherFeedbackDTO.FeedbackInfo> create(
            @RequestBody TeacherFeedbackDTO.CreateRequest req,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(feedbackService.createFeedback(req, currentUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeacherFeedbackDTO.FeedbackInfo> update(
            @PathVariable Long id,
            @RequestBody TeacherFeedbackDTO.UpdateRequest req,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(feedbackService.updateFeedback(id, req, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        feedbackService.deleteFeedback(id, currentUser);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{id}/mark-read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        feedbackService.markAsRead(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-received")
    public ResponseEntity<List<TeacherFeedbackDTO.FeedbackInfo>> getMyReceived(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(feedbackService.getMyReceivedFeedback(currentUser));
    }

    @GetMapping("/my-received/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal User currentUser) {
        long count = feedbackService.getUnreadCount(currentUser);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/my-given")
    public ResponseEntity<List<TeacherFeedbackDTO.FeedbackInfo>> getMyGiven(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(feedbackService.getMyGivenFeedback(currentUser));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<TeacherFeedbackDTO.FeedbackInfo>> getForStudent(
            @PathVariable Long studentId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(feedbackService.getFeedbackForStudent(studentId, currentUser));
    }

    @GetMapping("/attempt/{attemptId}")
    public ResponseEntity<List<TeacherFeedbackDTO.FeedbackInfo>> getForAttempt(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(feedbackService.getFeedbackForAttempt(attemptId, currentUser));
    }
}