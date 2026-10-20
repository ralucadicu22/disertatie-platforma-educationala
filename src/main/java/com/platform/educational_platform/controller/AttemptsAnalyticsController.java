package com.platform.educational_platform.controller;

import com.platform.educational_platform.dto.AttemptsAnalyticsDto.*;
import com.platform.educational_platform.service.AttemptsAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics/attempts")
@RequiredArgsConstructor
public class AttemptsAnalyticsController {

    private final AttemptsAnalyticsService attemptsAnalyticsService;

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyAuthority('ELEV', 'PROFESOR', 'ADMIN')")
    public ResponseEntity<StudentDashboard> getStudentDashboard(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(attemptsAnalyticsService.getStudentDashboard(studentId));
    }

    @GetMapping("/global")
    @PreAuthorize("hasAnyAuthority('PROFESOR', 'ADMIN')")
    public ResponseEntity<List<ClassAverageAttempts>> getClassAverage() {
        return ResponseEntity.ok(attemptsAnalyticsService.getClassAverage());
    }

    @GetMapping("/advanced")
    @PreAuthorize("hasAnyAuthority('PROFESOR', 'ADMIN')")
    public ResponseEntity<AdvancedAnalyticsResponse> getAdvancedAnalytics() {
        return ResponseEntity.ok(attemptsAnalyticsService.getAdvancedAnalytics());
    }
}