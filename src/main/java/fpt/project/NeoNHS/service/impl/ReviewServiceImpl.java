package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.ReviewRepository;
import fpt.project.NeoNHS.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
}
