package com.arturfrimu.materialecalculator;

import com.arturfrimu.materialecalculator.DTO.AllMaterialsResponse;
import com.arturfrimu.materialecalculator.DTO.CreateMaterialsRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ThymeleafController {
    private final MaterialRepository materialRepository;

    @GetMapping("/materials")
    public String getMaterialsView(Model model) {
        List<MaterialsEntity> materials = materialRepository.findAll();

        // Mapare din MaterialsEntity în AllMaterialsResponse
        List<AllMaterialsResponse> materialsResponse = materials.stream()
                .map(material -> new AllMaterialsResponse(
                        material.getId(),
                        material.getMaterialName(),
                        material.getPrice()))
                .collect(Collectors.toList());

        model.addAttribute("materials", materialsResponse);
        return "materials-view";
    }


    @PostMapping("/materials/adaugare")
    public String createMaterials(@ModelAttribute CreateMaterialsRequest materialsRequest) {
        MaterialsEntity newMaterial = new MaterialsEntity();
        newMaterial.setMaterialName(materialsRequest.getMaterialName());
        newMaterial.setPrice(materialsRequest.getPrice());

        materialRepository.save(newMaterial);

        return "redirect:/materials";
    }
    @PostMapping("/materials/update-price")
    public String updateMaterialPrice(@RequestParam Long materialId,
                                      @RequestParam BigDecimal newPrice,
                                      Model model) {
        System.out.println("Material ID: " + materialId);
        System.out.println("New Price: " + newPrice);

        // Răspunsul pentru actualizarea prețului
        materialRepository.findById(materialId)
                .map(material -> {
                    material.setPrice(newPrice);
                    materialRepository.save(material);
                    return material;
                })
                .orElseThrow(() -> new RuntimeException("Materialul nu a fost găsit"));

        List<MaterialsEntity> materials = materialRepository.findAll();
        List<AllMaterialsResponse> materialsResponse = materials.stream()
                .map(material -> new AllMaterialsResponse(
                        material.getId(),
                        material.getMaterialName(),
                        material.getPrice()))
                .collect(Collectors.toList());
        model.addAttribute("materials", materialsResponse);

        return "materials-view";
    }

    @PostMapping("/materials/delete")
    public String deleteMaterial(@RequestParam("deletematerialId") Long materialId, Model model) {
        materialRepository.findById(materialId)
                .ifPresentOrElse(
                        materialRepository::delete, // Șterge materialul dacă este găsit
                        () -> { throw new RuntimeException("Materialul nu a fost găsit"); } // Aruncă eroare dacă nu există
                );

        // Actualizăm lista materialelor după ștergere
        List<MaterialsEntity> materials = materialRepository.findAll();
        List<AllMaterialsResponse> materialsResponse = materials.stream()
                .map(material -> new AllMaterialsResponse(
                        material.getId(),
                        material.getMaterialName(),
                        material.getPrice()))
                .collect(Collectors.toList());
        model.addAttribute("materials", materialsResponse);

        return "materials-view"; // Redirecționăm către pagina cu materialele actualizate
    }

}
