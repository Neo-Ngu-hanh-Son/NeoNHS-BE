package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.BlogRepository;
import fpt.project.NeoNHS.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
}
