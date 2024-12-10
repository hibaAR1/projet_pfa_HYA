package com.exemple.pfa.controller;

import com.exemple.pfa.model.OcrResult;
import com.exemple.pfa.repository.OcrResultRepository;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ocr")
public class OcrController {

    @Autowired
    private OcrResultRepository ocrResultRepository;

    private final ITesseract tesseract;

    public OcrController() {
        this.tesseract = new Tesseract();
        // Set the path for tessdata
        this.tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata"); // Set your actual tessdata path
        // Default language setting to French, Arabic, and English
        this.tesseract.setLanguage("fra+ara+eng");
        this.tesseract.setTessVariable("user_defined_dpi", "300");
    }

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> extractData(@RequestParam("document") MultipartFile document,
                                                           @RequestParam(value = "language", defaultValue = "fra+ara+eng") String language) {
        try {
            // Set language dynamically based on the user input
            tesseract.setLanguage(language);

            // Read and process the image
            BufferedImage originalImage = ImageIO.read(document.getInputStream());
            BufferedImage processedImage = processImage(originalImage);

            // Perform OCR
            String text = tesseract.doOCR(processedImage);

            // Save OCR result
            OcrResult result = new OcrResult();
            result.setFileName(document.getOriginalFilename());
            result.setExtractedText(text);
            result.setProcessedAt(LocalDateTime.now());
            result.setContentType(document.getContentType());
            result.setFileSize(document.getSize());
            ocrResultRepository.save(result);

            // Build response
            Map<String, Object> response = new HashMap<>();
            response.put("id", result.getId());
            response.put("text", text);
            response.put("originalFileName", document.getOriginalFilename());
            response.put("contentType", document.getContentType());
            response.put("size", document.getSize());
            response.put("processedAt", result.getProcessedAt());

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error reading the file"));
        } catch (TesseractException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error extracting text from image"));
        }
    }

    private BufferedImage processImage(BufferedImage image) {
        // Convert the image to grayscale
        BufferedImage grayImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = grayImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return sharpenImage(grayImage);
    }

    private BufferedImage sharpenImage(BufferedImage image) {
        // Apply a sharpen filter
        float[] sharpenMatrix = {
                0f, -0.5f, 0f,
                -0.5f, 3f, -0.5f,
                0f, -0.5f, 0f
        };
        Kernel kernel = new Kernel(3, 3, sharpenMatrix);
        ConvolveOp convolveOp = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return convolveOp.filter(image, null);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OcrResult> getOcrResult(@PathVariable Long id) {
        OcrResult ocrResult = ocrResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OCR Result not found with id: " + id));
        return ResponseEntity.ok(ocrResult);
    }

    @GetMapping("/results")
    public List<OcrResult> getAllOcrResults() {
        return ocrResultRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOcrResult(@PathVariable Long id) {
        OcrResult ocrResult = ocrResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OCR Result not found with id: " + id));
        ocrResultRepository.delete(ocrResult);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<OcrResult> updateOcrResult(@PathVariable Long id, @RequestBody OcrResult updatedOcrResult) {
        OcrResult ocrResult = ocrResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("OCR Result not found with id: " + id));

        ocrResult.setFileName(updatedOcrResult.getFileName());
        ocrResult.setExtractedText(updatedOcrResult.getExtractedText());

        OcrResult savedOcrResult = ocrResultRepository.save(ocrResult);
        return ResponseEntity.ok(savedOcrResult);
    }

    @GetMapping("/search")
    public List<OcrResult> searchOcrResults(@RequestParam String fileName) {
        return ocrResultRepository.findByFileNameContaining(fileName);
    }
}