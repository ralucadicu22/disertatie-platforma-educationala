package com.platform.educational_platform.controller;

import com.platform.educational_platform.dto.BulkImportPreviewResponse;
import com.platform.educational_platform.model.ImportHistory;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.repository.UserRepository;
import com.platform.educational_platform.service.BulkImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/admin/bulk-import")
@RequiredArgsConstructor
public class BulkImportController {

    private final BulkImportService bulkImportService;
    private final UserRepository userRepository;

    @PostMapping("/preview")
    public ResponseEntity<BulkImportPreviewResponse> preview(
            @RequestParam("file") MultipartFile file) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(bulkImportService.preview(file));
    }

    @PostMapping("/import")
    public ResponseEntity<BulkImportPreviewResponse> importUsers(
            @RequestParam("file") MultipartFile file,
            Authentication auth) throws IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        User admin = resolveAdmin(auth);
        return ResponseEntity.ok(bulkImportService.importUsers(file, admin));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ImportHistory>> getHistory() {
        return ResponseEntity.ok(bulkImportService.getHistory());
    }

    @GetMapping("/template")
    public ResponseEntity<Resource> downloadTemplate() {
        String csv = "email,name,role\n" +
                "exemplu@email.ro,Nume Prenume,ELEV\n" +
                "profesor@email.ro,Profesor Exemplu,PROFESOR\n";
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        ByteArrayResource resource = new ByteArrayResource(bytes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"template_import.csv\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .contentLength(bytes.length)
                .body(resource);
    }

    private User resolveAdmin(Authentication auth) {
        if (auth.getPrincipal() instanceof User user) {
            return user;
        }
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Admin not found: " + email));
    }
}
