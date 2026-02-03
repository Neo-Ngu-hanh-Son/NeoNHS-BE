package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.attraction.CreateAttractionRequest;
import fpt.project.NeoNHS.entity.Attraction;
import fpt.project.NeoNHS.entity.Point;

import java.util.List;
import java.util.UUID;

public interface AttractionService {
    void createAttraction(CreateAttractionRequest request);

    List<Attraction> getAllAttractions();

    List<Point> getPointsByAttraction(UUID attractionId);
}
