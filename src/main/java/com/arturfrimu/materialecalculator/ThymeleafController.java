package com.arturfrimu.materialecalculator;

import com.arturfrimu.materialecalculator.DTO.AllMaterialsResponse;
import com.arturfrimu.materialecalculator.DTO.CreateMaterialsRequest;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
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

    @GetMapping("/materials/print-pdf")
    public ResponseEntity<byte[]> printMaterialsAsPdf(@RequestParam Map<String, String> productToCount) throws DocumentException {
        // Creează document PDF
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{1, 3, 3, 2, 3});

        // Adaugă antetul tabelului
        table.addCell("ID");
        table.addCell("Nume Produs");
        table.addCell("Pret");
        table.addCell("Cantitate");
        table.addCell("Pret Total");

        // Adaugă materialele
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<MaterialsEntity> materials = materialRepository.findAll();
        for (MaterialsEntity material : materials) {
            table.addCell(material.getId().toString());
            table.addCell(material.getMaterialName());
            table.addCell(material.getPrice().toString());

            // Obține cantitatea din parametrii introduși în formular
            String quantityStr = productToCount.get("productToCount[" + material.getId() + "]");
            int quantity = (quantityStr != null && !quantityStr.isEmpty()) ? Integer.parseInt(quantityStr) : 0;

            table.addCell(String.valueOf(quantity)); // Adaugă cantitatea

            // Calculează prețul total pentru produs
            BigDecimal productTotalPrice = material.getPrice().multiply(BigDecimal.valueOf(quantity));
            table.addCell(productTotalPrice.toString()); // Adaugă prețul total pentru produs

            // Adaugă la prețul total general
            totalPrice = totalPrice.add(productTotalPrice);
        }

        document.add(table);

        // Adaugă prețul total
        document.add(new Paragraph("Suma Totala: " + totalPrice.toString()));

        document.close();

        // Returnează PDF-ul
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "materials.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(out.toByteArray());
    }
}
