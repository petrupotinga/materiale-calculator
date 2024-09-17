package com.arturfrimu.materialecalculator;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialsEntity {
    private Long id;
    private String materialName;
    private BigDecimal price;

    public BigDecimal calculatePrice(BigDecimal count) {
        return price.multiply(count);
    }
}
