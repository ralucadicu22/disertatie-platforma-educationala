package com.platform.educational_platform.dto.exercise.version;

import lombok.Data;

@Data
public class SaveDraftRequest {
    private String config;
    private String type;
    private String changeNote;
}