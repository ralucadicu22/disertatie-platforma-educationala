package com.platform.educational_platform.repository;

import com.platform.educational_platform.model.ImportHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImportHistoryRepository extends JpaRepository<ImportHistory, Long> {
    List<ImportHistory> findTop20ByOrderByImportedAtDesc();
}
