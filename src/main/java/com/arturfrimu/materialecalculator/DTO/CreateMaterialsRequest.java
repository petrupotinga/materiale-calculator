package com.arturfrimu.materialecalculator.DTO;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMaterialsRequest {
    private Long id;
    private String materialName;
    private BigDecimal price;
}
