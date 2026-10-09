package com.platform.educational_platform.repository;

import com.platform.educational_platform.model.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    @Query(value = """
            SELECT * FROM activity_logs
            WHERE (:userId IS NULL OR user_id = :userId)
              AND (:action IS NULL OR action = :action)
              AND (:role IS NULL OR user_role = :role)
              AND (CAST(:fromDate AS timestamp) IS NULL OR timestamp >= CAST(:fromDate AS timestamp))
              AND (CAST(:toDate AS timestamp) IS NULL OR timestamp <= CAST(:toDate AS timestamp))
            ORDER BY timestamp DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM activity_logs
            WHERE (:userId IS NULL OR user_id = :userId)
              AND (:action IS NULL OR action = :action)
              AND (:role IS NULL OR user_role = :role)
              AND (CAST(:fromDate AS timestamp) IS NULL OR timestamp >= CAST(:fromDate AS timestamp))
              AND (CAST(:toDate AS timestamp) IS NULL OR timestamp <= CAST(:toDate AS timestamp))
            """,
            nativeQuery = true)
    Page<ActivityLog> search(
            @Param("userId") Long userId,
            @Param("action") String action,
            @Param("role") String role,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);

    @Query(value = "SELECT DISTINCT action FROM activity_logs ORDER BY action", nativeQuery = true)
    List<String> findDistinctActions();
}