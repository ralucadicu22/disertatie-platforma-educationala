package com.platform.educational_platform.repository;

import com.platform.educational_platform.model.ImpersonationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImpersonationLogRepository extends JpaRepository<ImpersonationLog, Long> {
    List<ImpersonationLog> findTop50ByOrderByTimestampDesc();
    List<ImpersonationLog> findByAdminIdOrderByTimestampDesc(Long adminId);
}
