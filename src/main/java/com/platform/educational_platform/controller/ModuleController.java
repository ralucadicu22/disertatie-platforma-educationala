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
    public ResponseEntity<List<ModuleDTO>> getModulesByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(moduleService.getModulesByProduct(productId));
    }

    @PostMapping("/product/{productId}")
    public ResponseEntity<ModuleDTO> createModule(
            @PathVariable Long productId,
            @RequestBody ModuleDTO dto) {
        return ResponseEntity.ok(moduleService.createModule(productId, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ModuleDTO> updateModule(
            @PathVariable Long id,
            @RequestBody ModuleDTO dto) {
        return ResponseEntity.ok(moduleService.updateModule(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteModule(@PathVariable Long id) {
        moduleService.deleteModule(id);
        return ResponseEntity.noContent().build();
    }
}