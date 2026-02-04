package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.point.PointRequest;
import fpt.project.NeoNHS.dto.response.point.PointResponse;
import fpt.project.NeoNHS.entity.Point;

import java.util.List;
import java.util.UUID;

public interface PointService {
    PointResponse createPoint(PointRequest request);
    PointResponse updatePoint(UUID id, PointRequest request);
    PointResponse deletePoint(UUID id);
    PointResponse getPointById(UUID id);
    List<PointResponse> getPointsByAttraction(UUID attractionId);
}
