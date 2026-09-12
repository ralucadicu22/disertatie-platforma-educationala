package com.platform.educational_platform.controller;

import com.platform.educational_platform.dto.UserProgressDTO;
import com.platform.educational_platform.dto.UserStatsDTO;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.service.UserProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class UserProgressController {

    private final UserProgressService userProgressService;

    @GetMapping("/me")
    public ResponseEntity<List<UserProgressDTO>> getMyHistory(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userProgressService.getUserHistory(currentUser.getId()));
    }

    @GetMapping("/me/stats")
    public ResponseEntity<UserStatsDTO> getMyStats(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(userProgressService.getUserStats(currentUser.getId()));
    }

    @GetMapping("/exercise/{exerciseId}")
    public ResponseEntity<List<UserProgressDTO>> getExerciseAttempts(
            @PathVariable Long exerciseId) {
        return ResponseEntity.ok(userProgressService.getExerciseAttempts(exerciseId));
    }

    @GetMapping("/teacher/all-students")
    public ResponseEntity<List<UserStatsDTO>> getAllStudentsStats() {
        return ResponseEntity.ok(userProgressService.getAllElevStats());
    }

    @GetMapping("/exercise/{exerciseId}/class-average")
    public ResponseEntity<Map<String, Double>> getClassAverage(
            @PathVariable Long exerciseId,
            @AuthenticationPrincipal User currentUser) {
        Double classAvg = userProgressService.getClassAverageForExercise(exerciseId, currentUser.getId());
        Double globalAvg = userProgressService.getGlobalAverageForExercise(exerciseId);
        return ResponseEntity.ok(Map.of(
                "classAverage", classAvg,
                "globalAverage", globalAvg
        ));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<UserProgressDTO>> getStudentHistory(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(userProgressService.getUserHistory(studentId));
    }

    @GetMapping("/student/{studentId}/stats")
    public ResponseEntity<UserStatsDTO> getStudentStats(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(userProgressService.getUserStats(studentId));
    }
}