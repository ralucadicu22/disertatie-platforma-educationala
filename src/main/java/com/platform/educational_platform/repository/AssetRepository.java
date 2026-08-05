package com.platform.educational_platform.repository;

import com.platform.educational_platform.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findAllByOrderByCreatedAtDesc();
    List<Asset> findByMimeTypeStartingWithOrderByCreatedAtDesc(String mimeTypePrefix);
}