package com.platform.educational_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetDTO {
    private Long id;
    private String originalName;
    private String url;
    private String mimeType;
    private Long sizeBytes;
    private String uploadedByName;
    private LocalDateTime createdAt;
}