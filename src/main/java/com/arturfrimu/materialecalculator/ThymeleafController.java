package com.arturfrimu.materialecalculator;

import com.arturfrimu.materialecalculator.DTO.AllMaterialsResponse;
import com.arturfrimu.materialecalculator.DTO.CreateMaterialsRequest;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
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
import java.util.stream.Stream;

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

    @GetMapping("/materials/delete")
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
        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        // Setăm fonturile
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new int[]{3, 3, 2, 3});

        // Stilizăm antetul tabelului
        Stream.of("Nume Produs", "Pret", "Cantitate", "Pret Total")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.DARK_GRAY);
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    header.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    header.setPadding(5);
                    header.setPhrase(new Phrase(columnTitle, headerFont));
                    table.addCell(header);
                });

        // Adăugăm materialele în tabel cu alternarea culorilor
        BigDecimal totalPrice = BigDecimal.ZERO;
        List<MaterialsEntity> materials = materialRepository.findAll();
        int rowIndex = 0;  // Index pentru a alterna culorile rândurilor

        for (MaterialsEntity material : materials) {
            // Obținem cantitatea pentru produs
            String quantityStr = productToCount.get("productToCount[" + material.getId() + "]");
            int quantity = (quantityStr != null && !quantityStr.isEmpty()) ? Integer.parseInt(quantityStr) : 0;

            // Dacă cantitatea este 0, trecem la următorul produs
            if (quantity == 0) {
                continue;
            }

            // Alternăm culoarea de fundal între gri deschis și alb
            BaseColor backgroundColor = (rowIndex % 2 == 0) ? BaseColor.LIGHT_GRAY : BaseColor.WHITE;

            // Nume produs
            PdfPCell nameCell = new PdfPCell(new Phrase(material.getMaterialName(), cellFont));
            nameCell.setPadding(5);
            nameCell.setBackgroundColor(backgroundColor);
            table.addCell(nameCell);

            // Preț
            PdfPCell priceCell = new PdfPCell(new Phrase(material.getPrice().toString(), cellFont));
            priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            priceCell.setPadding(5);
            priceCell.setBackgroundColor(backgroundColor);
            table.addCell(priceCell);

            // Cantitate
            PdfPCell quantityCell = new PdfPCell(new Phrase(String.valueOf(quantity), cellFont));
            quantityCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            quantityCell.setPadding(5);
            quantityCell.setBackgroundColor(backgroundColor);
            table.addCell(quantityCell);

            // Preț Total pentru produs
            BigDecimal productTotalPrice = material.getPrice().multiply(BigDecimal.valueOf(quantity));
            PdfPCell totalCell = new PdfPCell(new Phrase(productTotalPrice.toString(), cellFont));
            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalCell.setPadding(5);
            totalCell.setBackgroundColor(backgroundColor);
            table.addCell(totalCell);

            // Calculăm suma totală
            totalPrice = totalPrice.add(productTotalPrice);
            rowIndex++;  // Incrementăm indexul pentru a alterna culorile
        }
        document.add(table);
        // Adăugăm suma totală generală
        Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
        Paragraph totalParagraph = new Paragraph("Suma Totala: " + totalPrice.toString(), totalFont);
        totalParagraph.setAlignment(Element.ALIGN_RIGHT);
        totalParagraph.setSpacingBefore(10);
        document.add(totalParagraph);

        document.close();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "materials.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(out.toByteArray());
    }
}
