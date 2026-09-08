package com.platform.educational_platform.controller;

import com.platform.educational_platform.dto.HintDTO;
import com.platform.educational_platform.service.ExerciseHintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercises/{exerciseId}/hints")
@RequiredArgsConstructor
public class ExerciseHintController {

    private final ExerciseHintService hintService;

    @GetMapping("/available")
    public ResponseEntity<List<HintDTO>> getAvailable(
            @PathVariable Long exerciseId,
            @RequestParam(defaultValue = "0") int mistakes) {
        return ResponseEntity.ok(hintService.getAvailableHints(exerciseId, mistakes));
    }
    @GetMapping("/all")
    public ResponseEntity<List<HintDTO>> getAll(@PathVariable Long exerciseId) {
        return ResponseEntity.ok(hintService.getAllHints(exerciseId));
    }
}