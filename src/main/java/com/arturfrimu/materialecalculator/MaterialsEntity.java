package com.arturfrimu.materialecalculator;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "materials")
public class MaterialsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String materialName;
    private BigDecimal price;

    public BigDecimal calculatePrice(BigDecimal count) {
        return price.multiply(count);
    }
}