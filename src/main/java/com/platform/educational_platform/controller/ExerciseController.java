package com.platform.educational_platform.controller;

import com.platform.educational_platform.dto.AnswerRequest;
import com.platform.educational_platform.dto.exercise.ExerciseDTO;
import com.platform.educational_platform.dto.exercise.FeedbackResponse;
import com.platform.educational_platform.service.ExerciseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ExerciseController {

    private final ExerciseService exerciseService;

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<List<ExerciseDTO>> getExercisesByQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(exerciseService.getExercisesByQuiz(quizId));
    }

    @PostMapping("/submit")
    public ResponseEntity<FeedbackResponse> submitAnswer(
            @RequestBody AnswerRequest request,
            Authentication authentication) {
        String userEmail = (String) authentication.getPrincipal();
        return ResponseEntity.ok(exerciseService.checkAnswer(request, userEmail));
    }
    @GetMapping("/{id}")
    public ResponseEntity<ExerciseDTO> getExerciseById(@PathVariable Long id) {
        return ResponseEntity.ok(exerciseService.getExerciseById(id));
    }
}