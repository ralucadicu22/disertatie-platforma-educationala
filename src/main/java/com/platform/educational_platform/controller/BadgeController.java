package com.platform.educational_platform.controller;

import com.platform.educational_platform.dto.BadgeDTO;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class BadgeController {

    private final BadgeService badgeService;

    @GetMapping("/my")
    public ResponseEntity<List<BadgeDTO>> getMyBadges(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(badgeService.getUserBadges(currentUser.getId()));
    }
}