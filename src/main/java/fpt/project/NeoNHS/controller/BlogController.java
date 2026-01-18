package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.service.BlogCategoryService;
import fpt.project.NeoNHS.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;
    private final BlogCategoryService blogCategoryService;
}
