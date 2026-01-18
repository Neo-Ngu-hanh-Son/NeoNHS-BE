package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.ReviewImageRepository;
import fpt.project.NeoNHS.service.ReviewImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewImageServiceImpl implements ReviewImageService {

    private final ReviewImageRepository reviewImageRepository;
}
