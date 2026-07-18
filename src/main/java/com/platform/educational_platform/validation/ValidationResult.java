package com.platform.educational_platform.validation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {

    private boolean correct;
    private double score;
    private Map<String, Object> details;
}