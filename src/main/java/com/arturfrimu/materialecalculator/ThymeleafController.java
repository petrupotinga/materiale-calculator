package com.arturfrimu.materialecalculator;

import com.arturfrimu.materialecalculator.DTO.CreateMaterialsRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ThymeleafController {
    private final MaterialRepository materialRepository;

    @GetMapping("/materials")
    public String getMaterialsView(Model model) {
        List<MaterialsEntity> materials = materialRepository.findAll();
        model.addAttribute("materials", materials);
        return "materials-view";
    }

    @PostMapping("/materials")
    public String createMaterials(@ModelAttribute CreateMaterialsRequest materialsRequest) {
        MaterialsEntity newMaterial = new MaterialsEntity();
        newMaterial.setMaterialName(materialsRequest.getMaterialName());
        newMaterial.setPrice(materialsRequest.getPrice());

        materialRepository.save(newMaterial);

        return "redirect:/materials";
    }
}
