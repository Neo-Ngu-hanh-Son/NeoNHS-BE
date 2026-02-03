package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.request.attraction.CreateAttractionRequest;
import fpt.project.NeoNHS.entity.Attraction;
import fpt.project.NeoNHS.entity.Point;
import fpt.project.NeoNHS.service.AttractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attractions")
@RequiredArgsConstructor
public class AttractionController {

    private final AttractionService attractionService;

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> createAttraction(@RequestBody CreateAttractionRequest request) {
        attractionService.createAttraction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Attraction created successfully!");
    }

    @GetMapping("/get-all-attraction")
    public ResponseEntity<List<Attraction>> getAllAttractions() {
        List<Attraction> attractions = attractionService.getAllAttractions();
        return ResponseEntity.ok(attractions);
    }

    @GetMapping("/{attractionId}/points")
    public ResponseEntity<List<Point>> getPointsByAttraction(@PathVariable UUID attractionId) {
        List<Point> points = attractionService.getPointsByAttraction(attractionId);
        return ResponseEntity.ok(points);
    }
}
