package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.StatisticsResponse;
import fpt.project.NeoNHS.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Public statistics APIs")
public class StatisticsController {

  private final StatisticsService statisticsService;

  @Operation(summary = "Get counts", description = "Get total counts of blogs, workshops, events, and attractions")
  @GetMapping("/counts")
  public ApiResponse<StatisticsResponse> getCounts() {
    StatisticsResponse data = statisticsService.getCounts();
    return ApiResponse.success("Counts retrieved successfully", data);
  }
}
