package com.platform.educational_platform.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "import_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long adminId;

    @Column(nullable = false)
    private String adminEmail;

    @Column(nullable = false)
    private String adminName;

    private String fileName;

    @Column(nullable = false)
    private LocalDateTime importedAt;

    private int totalRows;
    private int successCount;
    private int errorCount;
    private int skippedCount;
    @Column(columnDefinition = "TEXT")
    private String errorsJson;
}
