package com.arturfrimu.materialecalculator;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class MaterialController {

    @PostConstruct
    public void postConstruct() {
        materialRepository.addMaterials(new MaterialsEntity(1L, "Scanduri", BigDecimal.valueOf(1)));
        materialRepository.addMaterials(new MaterialsEntity(2L, "Chiroane", BigDecimal.valueOf(5)));
        materialRepository.addMaterials(new MaterialsEntity(3L, "Tinte", BigDecimal.valueOf(10)));
        materialRepository.addMaterials(new MaterialsEntity(4L, "Table", BigDecimal.valueOf(20)));
    }

    MaterialRepository materialRepository;

    @GetMapping
    public List<MaterialsEntity> findAllMaterials() {
        return materialRepository.findAll();
    }

    @PostMapping
    public BigDecimal calculator(@RequestBody Map<Long, BigDecimal> productToCount) {
        return productToCount.entrySet()
                .stream()
                .map(
                        entry -> materialRepository.findById(entry.getKey())
                                .map(entity -> entity.calculatePrice(entry.getValue()))
                                .orElse(BigDecimal.ZERO)
                ).reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }
}
