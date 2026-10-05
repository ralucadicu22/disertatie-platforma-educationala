package com.platform.educational_platform.controller;

import com.platform.educational_platform.dto.exercisetype.ExerciseTypeConfigDTO;
import com.platform.educational_platform.dto.exercisetype.ProductTypeOverrideDTO;
import com.platform.educational_platform.dto.exercisetype.ResolvedTypeStatusDTO;
import com.platform.educational_platform.service.ExerciseTypeConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exercise-types")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ExerciseTypeConfigController {

    private final ExerciseTypeConfigService service;
    @GetMapping("/config")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<List<ExerciseTypeConfigDTO>> getAllGlobalConfigs() {
        return ResponseEntity.ok(service.getAllGlobalConfigs());
    }

    @PutMapping("/config/{exerciseType}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ExerciseTypeConfigDTO> setGlobalEnabled(
            @PathVariable String exerciseType,
            @RequestBody Map<String, Boolean> body) {
        Boolean enabled = body.get("enabled");
        if (enabled == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(service.setGlobalEnabled(exerciseType, enabled));
    }

    @GetMapping("/product/{productId}/overrides")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<List<ProductTypeOverrideDTO>> getOverridesForProduct(
            @PathVariable Long productId) {
        return ResponseEntity.ok(service.getOverridesForProduct(productId));
    }

    @PutMapping("/product/{productId}/{exerciseType}")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<ProductTypeOverrideDTO> setProductOverride(
            @PathVariable Long productId,
            @PathVariable String exerciseType,
            @RequestBody Map<String, Object> body) {
        Boolean enabled = null;
        if (body.containsKey("enabled") && body.get("enabled") != null) {
            enabled = (Boolean) body.get("enabled");
        }
        return ResponseEntity.ok(service.setProductOverride(productId, exerciseType, enabled));
    }

    @GetMapping("/product/{productId}/resolved")
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    public ResponseEntity<List<ResolvedTypeStatusDTO>> getResolvedStatusForProduct(
            @PathVariable Long productId) {
        return ResponseEntity.ok(service.getResolvedStatusForProduct(productId));
    }

    @GetMapping("/allowed")
    @PreAuthorize("hasAnyAuthority('EDITOR', 'ADMIN')")
    public ResponseEntity<List<String>> getAllowedTypesForQuiz(
            @RequestParam Long quizId) {
        List<String> allKnownTypes = List.of(
                "CUSTOM", "FIND_AND_SELECT", "FIND_AND_COUNT", "DRAG_AND_DROP",
                "MULTIPLE_CHOICE", "INPUT_TEXT", "INPUT_NUMERIC", "MATCHING",
                "ORDERING", "PUZZLE", "COLORING", "GRID_BASED", "VIDEO", "PDF",
                "TABLES_CHARTS", "LOGICAL_SERIES", "TEXT_PROBLEMS"
        );
        List<String> allowed = allKnownTypes.stream()
                .filter(type -> service.isTypeAllowedForQuiz(quizId, type))
                .toList();
        return ResponseEntity.ok(allowed);
    }
}
