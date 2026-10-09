package com.platform.educational_platform.controller;

import com.platform.educational_platform.model.ActivityLog;
import com.platform.educational_platform.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/activity-logs")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class ActivityLogController {

    private final ActivityLogRepository activityLogRepository;

    @GetMapping
    public Page<ActivityLog> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String role,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {

        Pageable pageable = PageRequest.of(page, Math.min(size, 200));
        return activityLogRepository.search(userId, blankToNull(action), blankToNull(role), fromDate, toDate, pageable);
    }

    @GetMapping("/actions")
    public List<String> listActions() {
        return activityLogRepository.findDistinctActions();
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}