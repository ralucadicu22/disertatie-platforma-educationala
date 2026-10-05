package com.platform.educational_platform.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "product_type_override",
        uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "exercise_type"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductTypeOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "exercise_type", nullable = false, length = 50)
    private String exerciseType;

    @Column(name = "enabled")
    private Boolean enabled;
}
