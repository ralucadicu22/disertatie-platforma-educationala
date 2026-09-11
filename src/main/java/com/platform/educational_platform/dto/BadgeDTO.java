package com.platform.educational_platform.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BadgeDTO {
    private Long id;
    private String name;
    private String icon;
    private String description;
    private String criteria;
    private LocalDateTime earnedAt;
}