package com.platform.educational_platform.dto.exercisetype;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductTypeOverrideDTO {
    private Long id;
    private Long productId;
    private String exerciseType;
    private Boolean enabled;
}
