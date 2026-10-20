package com.platform.educational_platform.controller;


import com.platform.educational_platform.dto.AtRiskDTO;
import com.platform.educational_platform.dto.RecommendationDTO;
import com.platform.educational_platform.service.AtRiskAndRecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AtRiskAndRecommendationController {

    private final AtRiskAndRecommendationService atRiskService;


    @GetMapping("/at-risk/students")
    @PreAuthorize("hasAnyAuthority('PROFESOR', 'ADMIN')")
    public ResponseEntity<List<AtRiskDTO.AtRiskStudent>> getAtRiskStudents() {
        return ResponseEntity.ok(atRiskService.getAtRiskStudents());
    }

    @GetMapping("/recommendations/student/{studentId}")
    @PreAuthorize("hasAnyAuthority('ELEV', 'PROFESOR', 'ADMIN')")
    public ResponseEntity<RecommendationDTO.StudentRecommendations> getRecommendations(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(atRiskService.getRecommendationsForStudent(studentId));
    }
}
