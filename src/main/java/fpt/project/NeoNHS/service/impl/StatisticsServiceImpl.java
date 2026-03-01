package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.response.StatisticsResponse;
import fpt.project.NeoNHS.repository.AttractionRepository;
import fpt.project.NeoNHS.repository.BlogRepository;
import fpt.project.NeoNHS.repository.EventRepository;
import fpt.project.NeoNHS.repository.PointRepository;
import fpt.project.NeoNHS.repository.WorkshopTemplateRepository;
import fpt.project.NeoNHS.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

  private final BlogRepository blogRepository;
  private final WorkshopTemplateRepository workshopTemplateRepository;
  private final EventRepository eventRepository;
  private final AttractionRepository attractionRepository;
  private final PointRepository pointRepository;

  @Override
  public StatisticsResponse getCounts() {
    return StatisticsResponse.builder()
        .blogCount(blogRepository.countByDeletedAtIsNull())
        .workshopCount(workshopTemplateRepository.countByDeletedAtIsNull())
        .eventCount(eventRepository.countByDeletedAtIsNull())
        .attractionCount(attractionRepository.countByDeletedAtIsNull())
        .pointCount(pointRepository.countByDeletedAtIsNull())
        .build();
  }
}
