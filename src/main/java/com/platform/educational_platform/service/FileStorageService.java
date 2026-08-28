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
    private int maxImageSizeMb;

    @Value("${app.upload.max-video-size-mb:50}")
    private int maxVideoSizeMb;

    private static final java.util.Set<String> ALLOWED_IMAGE_TYPES = java.util.Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml"
    );

    private static final java.util.Set<String> ALLOWED_VIDEO_TYPES = java.util.Set.of(
            "video/mp4", "video/webm", "video/ogg"
    );
    private static final java.util.Set<String> ALLOWED_PDF_TYPES = java.util.Set.of(
            "application/pdf"
    );

    public String store(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("Fisierul este gol");
        }

        String mimeType = file.getContentType();
        boolean isImage = mimeType != null && ALLOWED_IMAGE_TYPES.contains(mimeType);
        boolean isVideo = mimeType != null && ALLOWED_VIDEO_TYPES.contains(mimeType);

        boolean isPdf = mimeType != null && ALLOWED_PDF_TYPES.contains(mimeType);

        if (!isImage && !isVideo && !isPdf) {
            throw new RuntimeException("Tip de fisier nepermis: " + mimeType +
                    ". Acceptate: imagini, video (MP4/WebM/OGG), PDF");
        }

        int maxSizeMb = isVideo ? maxVideoSizeMb : maxImageSizeMb;
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