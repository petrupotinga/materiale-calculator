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


    @PostMapping("/materials")
    public String createMaterials(@ModelAttribute CreateMaterialsRequest materialsRequest) {
        MaterialsEntity newMaterial = new MaterialsEntity();
        newMaterial.setMaterialName(materialsRequest.getMaterialName());
        newMaterial.setPrice(materialsRequest.getPrice());

        materialRepository.save(newMaterial);

        return "redirect:/materials";
    }

    @PostMapping("/materials/calculate")
    public String calculator(@RequestParam Map<String, String> productToCount, Model model) {
        System.out.println("Produse primite pentru calculare: " + productToCount.toString());

        // Obținem toate materialele pentru afișare în tabel
        List<MaterialsEntity> materials = materialRepository.findAll();
        List<AllMaterialsResponse> materialsResponse = materials.stream()
                .map(material -> new AllMaterialsResponse(
                        material.getId(),
                        material.getMaterialName(),
                        material.getPrice()))
                .collect(Collectors.toList());
        model.addAttribute("materials", materialsResponse);  // Afișăm din nou materialele

        // Calculăm prețul total
        BigDecimal totalPrice = productToCount.entrySet()
                .stream()
                .map(entry -> {
                    System.out.println("Cheie primită: " + entry.getKey() + ", Valoare: " + entry.getValue()); // Debugging
                    try {
                        // Extragem doar ID-ul materialului din cheia 'productToCount[1]'
                        String key = entry.getKey().replaceAll("[^0-9]", ""); // Eliminăm orice caracter care nu este cifră
                        Long id = Long.valueOf(key); // Conversia din String în Long

                        return materialRepository.findById(id)
                                .map(entity -> entity.calculatePrice(new BigDecimal(entry.getValue())))
                                .orElse(BigDecimal.ZERO);
                    } catch (NumberFormatException e) {
                        System.out.println("Eroare de conversie pentru cheia: " + entry.getKey()); // Debugging
                        return BigDecimal.ZERO; // Dacă conversia eșuează, returnăm zero
                    }
                }).reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);

        model.addAttribute("totalPrice", totalPrice);  // Adăugăm prețul total în model
        return "materials-view";  // Rămânem pe aceeași pagină
    }


}
