package com.platform.educational_platform.repository;

import com.platform.educational_platform.model.Badge;
import com.platform.educational_platform.model.enums.BadgeCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {
    List<Badge> findByUserIdOrderByEarnedAtDesc(Long userId);
    boolean existsByUserIdAndCriteria(Long userId, BadgeCriteria criteria);
    long countByUserId(Long userId);
}