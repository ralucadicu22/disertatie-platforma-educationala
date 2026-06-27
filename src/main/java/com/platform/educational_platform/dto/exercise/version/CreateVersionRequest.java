package com.platform.educational_platform.dto.exercise.version;

import lombok.Data;

@Data
public class CreateVersionRequest {
    private String config;
    private String changeNote;
}