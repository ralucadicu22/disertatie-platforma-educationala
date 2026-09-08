package com.platform.educational_platform.controller;

import com.platform.educational_platform.dto.QuizAssignmentDTO;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.service.QuizAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-assignments")
@RequiredArgsConstructor
public class QuizAssignmentController {

    private final QuizAssignmentService quizAssignmentService;

    @PostMapping("/assign")
    public ResponseEntity<Void> assign(
            @RequestBody QuizAssignmentDTO.AssignRequest req,
            @AuthenticationPrincipal User currentUser) {
        quizAssignmentService.assignQuizzesToClassrooms(req, currentUser);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/classroom/{classroomId}/quiz/{quizId}")
    public ResponseEntity<Void> unassign(
            @PathVariable Long classroomId,
            @PathVariable Long quizId,
            @AuthenticationPrincipal User currentUser) {
        quizAssignmentService.unassignQuiz(classroomId, quizId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-classrooms")
    public ResponseEntity<List<QuizAssignmentDTO.ClassroomQuizzes>> getMyClassroomsWithQuizzes(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(quizAssignmentService.getMyClassroomsWithQuizzes(currentUser));
    }

    @GetMapping("/my-quizzes")
    public ResponseEntity<List<QuizAssignmentDTO.AssignedQuizInfo>> getMyAssignedQuizzes(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(quizAssignmentService.getMyAssignedQuizzes(currentUser));
    }
}