package com.platform.educational_platform.controller;

import com.platform.educational_platform.dto.ParentDashboardDTO;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.service.ParentDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/parent-dashboard")
@RequiredArgsConstructor
public class ParentDashboardController {

    private final ParentDashboardService parentDashboardService;

    @GetMapping("/overview")
    public ResponseEntity<ParentDashboardDTO.Overview> getOverview(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(parentDashboardService.getOverview(currentUser));
    }

    @GetMapping("/child/{childId}")
    public ResponseEntity<ParentDashboardDTO.ChildDetail> getChildDetail(
            @PathVariable Long childId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(parentDashboardService.getChildDetail(childId, currentUser));
    }
}