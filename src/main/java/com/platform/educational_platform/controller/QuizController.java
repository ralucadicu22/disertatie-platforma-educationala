package com.platform.educational_platform.controller;

import com.platform.educational_platform.dto.QuizDTO;
import com.platform.educational_platform.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class QuizController {

    private final QuizService quizService;

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<QuizDTO>> getQuizzesByModule(@PathVariable Long moduleId) {
        return ResponseEntity.ok(quizService.getQuizzesByModule(moduleId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizDTO> getQuizById(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.getQuizById(id));
    }
    @GetMapping
    public ResponseEntity<List<QuizDTO>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }
}