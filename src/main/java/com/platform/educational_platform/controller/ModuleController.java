package com.platform.educational_platform.controller;

import com.platform.educational_platform.dto.ModuleDTO;
import com.platform.educational_platform.service.ModuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class ModuleController {

    private final ModuleService moduleService;

    @GetMapping
    public ResponseEntity<List<ModuleDTO>> getAllModules() {
        return ResponseEntity.ok(moduleService.getAllModules());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ModuleDTO> getModuleById(@PathVariable Long id) {
        return ResponseEntity.ok(moduleService.getModuleById(id));
    }
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ModuleDTO>> getModulesByProduct(
            @PathVariable Long productId) {
        return ResponseEntity.ok(moduleService.getModulesByProduct(productId));
    }
}