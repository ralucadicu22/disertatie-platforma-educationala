package com.platform.educational_platform.controller;

import com.platform.educational_platform.dto.QuizAttemptDTO;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.service.QuizAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz-attempts")
@RequiredArgsConstructor
public class QuizAttemptController {

    private final QuizAttemptService quizAttemptService;

    @PostMapping("/start/{quizId}")
    public ResponseEntity<QuizAttemptDTO.StartResponse> start(
            @PathVariable Long quizId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(quizAttemptService.startQuiz(quizId, currentUser));
    }
    @PostMapping("/{attemptId}/finish")
    public ResponseEntity<QuizAttemptDTO.FinishResponse> finish(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(quizAttemptService.finishQuiz(attemptId, currentUser));
    }

    @GetMapping("/quiz/{quizId}/history")
    public ResponseEntity<QuizAttemptDTO.QuizHistory> getHistory(
            @PathVariable Long quizId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(quizAttemptService.getMyQuizHistory(quizId, currentUser));
    }
}