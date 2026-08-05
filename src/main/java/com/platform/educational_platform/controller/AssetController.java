package com.platform.educational_platform.controller;

import com.platform.educational_platform.dto.AssetDTO;
import com.platform.educational_platform.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class AssetController {

    private final AssetService assetService;

    @PostMapping("/upload")
    public ResponseEntity<AssetDTO> upload(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {
        return ResponseEntity.ok(assetService.upload(file, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<List<AssetDTO>> list(
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(assetService.listAll(type));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws IOException {
        assetService.delete(id);
        return ResponseEntity.noContent().build();
    }
}