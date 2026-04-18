package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.service.TranslationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/translate")
@RequiredArgsConstructor
public class TranslationController {

    private final TranslationService translationService;

    @PostMapping("/text")
    public ResponseEntity<Map<String, String>> translateText(
        @RequestBody Map<String, String> request
    ) {
        String translated = translationService.translate(
            request.get("text"), request.get("targetLang")
        );
        return ResponseEntity.ok(Map.of("result", translated));
    }

    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> translateBatch(
        @RequestBody Map<String, Object> request
    ) {
        String targetLang = (String) request.get("targetLang");
        @SuppressWarnings("unchecked")
        Map<String, String> fields = (Map<String, String>) request.get("fields");
        Map<String, String> translated = translationService.translateFields(fields, targetLang);
        return ResponseEntity.ok(Map.of(
            "targetLang", targetLang,
            "translations", translated
        ));
    }
}
