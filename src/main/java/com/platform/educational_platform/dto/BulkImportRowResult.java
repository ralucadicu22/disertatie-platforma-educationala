package com.platform.educational_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkImportRowResult {

    private int rowNumber;
    private String email;
    private String name;
    private String role;
    private String status;
    private String errorMessage;
    private String tempPassword;
}
