package com.arturfrimu.materialecalculator;

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

    MaterialRepository materialRepository;
//
//    @PostMapping("/materials") // Endpoint pentru adăugarea materialelor
//    public MaterialsEntity addMaterial(@RequestBody MaterialsEntity material) {
//        return materialRepository.save(material); // Salvează materialul în baza de date
//    }
//
//    @GetMapping("/materials")
//    public List<MaterialsEntity> findAllMaterials() {
//        return materialRepository.findAll();
//    }

//    @PostMapping("/calculate")
//    public BigDecimal calculator(@RequestBody Map<Long, BigDecimal> productToCount) {
//        return productToCount.entrySet()
//                .stream()
//                .map(
//                        entry -> materialRepository.findById(entry.getKey())
//                                .map(entity -> entity.calculatePrice(entry.getValue()))
//                                .orElse(BigDecimal.ZERO)
//                ).reduce(BigDecimal::add)
//                .orElse(BigDecimal.ZERO);
//    }
}
