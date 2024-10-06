package com.arturfrimu.materialecalculator.DTO;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllMaterialsResponse {
    private Long id;
    private String materialName;
    private BigDecimal price;
}
