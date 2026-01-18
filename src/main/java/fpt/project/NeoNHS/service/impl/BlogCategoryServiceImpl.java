package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.BlogCategoryRepository;
import fpt.project.NeoNHS.service.BlogCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlogCategoryServiceImpl implements BlogCategoryService {

    private final BlogCategoryRepository blogCategoryRepository;
}
