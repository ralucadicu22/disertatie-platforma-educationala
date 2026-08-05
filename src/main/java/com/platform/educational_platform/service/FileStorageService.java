package com.platform.educational_platform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.max-size-mb:10}")
    private int maxSizeMb;

    private static final java.util.Set<String> ALLOWED_TYPES = java.util.Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml"
    );

    public String store(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("Fisierul este gol");
        }

        String mimeType = file.getContentType();
        if (mimeType == null || !ALLOWED_TYPES.contains(mimeType)) {
            throw new RuntimeException("Tip de fisier nepermis: " + mimeType +
                    ". Acceptate: JPEG, PNG, GIF, WebP, SVG");
        }

        long maxBytes = (long) maxSizeMb * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new RuntimeException("Fisierul depaseste limita de " + maxSizeMb + "MB");
        }

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        String originalFilename = file.getOriginalFilename() != null
                ? file.getOriginalFilename() : "file";
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String storedName = UUID.randomUUID() + extension;
        Path targetPath = uploadPath.resolve(storedName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return storedName;
    }

    public void delete(String storedName) throws IOException {
        Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(storedName);
        Files.deleteIfExists(filePath);
    }
}