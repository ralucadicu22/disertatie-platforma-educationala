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
}