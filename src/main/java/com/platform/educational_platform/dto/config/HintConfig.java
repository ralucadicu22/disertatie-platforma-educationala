package com.platform.educational_platform.dto.config;

import lombok.Data;

@Data
public class HintConfig {
    private String text;
    private int afterAttempt;
    private String type;
}