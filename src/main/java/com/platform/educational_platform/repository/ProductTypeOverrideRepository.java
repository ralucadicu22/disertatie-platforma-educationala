package com.platform.educational_platform.repository;

import com.platform.educational_platform.model.ProductTypeOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductTypeOverrideRepository extends JpaRepository<ProductTypeOverride, Long> {
    List<ProductTypeOverride> findByProductId(Long productId);
    Optional<ProductTypeOverride> findByProductIdAndExerciseType(Long productId, String exerciseType);
    void deleteByProductIdAndExerciseType(Long productId, String exerciseType);
}
