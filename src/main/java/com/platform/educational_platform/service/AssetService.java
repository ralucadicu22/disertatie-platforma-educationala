package com.platform.educational_platform.service;

import com.platform.educational_platform.dto.AssetDTO;
import com.platform.educational_platform.model.Asset;
import com.platform.educational_platform.model.User;
import com.platform.educational_platform.repository.AssetRepository;
import com.platform.educational_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public AssetDTO upload(MultipartFile file, String uploaderEmail) throws IOException {
        User uploader = userRepository.findByEmail(uploaderEmail)
                .orElseThrow(() -> new RuntimeException("Utilizator inexistent"));

        String storedName = fileStorageService.store(file);
        String url = "/uploads/" + storedName;

        Asset asset = Asset.builder()
                .originalName(file.getOriginalFilename())
                .storedName(storedName)
                .url(url)
                .mimeType(file.getContentType())
                .sizeBytes(file.getSize())
                .uploadedBy(uploader)
                .build();

        return toDTO(assetRepository.save(asset));
    }

    public List<AssetDTO> listAll(String typeFilter) {
        if (typeFilter != null && !typeFilter.isBlank()) {
            return assetRepository
                    .findByMimeTypeStartingWithOrderByCreatedAtDesc(typeFilter)
                    .stream().map(this::toDTO).collect(Collectors.toList());
        }
        return assetRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public void delete(Long assetId) throws IOException {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new RuntimeException("Asset-ul nu exista"));
        fileStorageService.delete(asset.getStoredName());
        assetRepository.delete(asset);
    }

    private AssetDTO toDTO(Asset a) {
        return AssetDTO.builder()
                .id(a.getId())
                .originalName(a.getOriginalName())
                .url(a.getUrl())
                .mimeType(a.getMimeType())
                .sizeBytes(a.getSizeBytes())
                .uploadedByName(a.getUploadedBy() != null ? a.getUploadedBy().getName() : null)
                .createdAt(a.getCreatedAt())
                .build();
    }
}