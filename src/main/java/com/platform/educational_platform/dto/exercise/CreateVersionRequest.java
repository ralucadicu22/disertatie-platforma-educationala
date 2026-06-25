package com.platform.educational_platform.dto.exercise;

import lombok.Data;

@Data
public class CreateVersionRequest {
    private String config;
    private String changeNote;
}