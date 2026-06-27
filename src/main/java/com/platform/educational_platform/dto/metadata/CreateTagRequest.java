package com.platform.educational_platform.dto.metadata;

import lombok.Data;

@Data
public class CreateTagRequest {
    private String name;
    private String description;
}