package com.platform.educational_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkImportPreviewResponse {

    private int totalRows;
    private int validCount;
    private int invalidCount;
    private int duplicateCount;

    private List<BulkImportRowResult> rows;
}
