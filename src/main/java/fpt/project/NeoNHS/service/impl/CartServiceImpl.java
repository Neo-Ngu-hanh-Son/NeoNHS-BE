package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.CartRepository;
import fpt.project.NeoNHS.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
}
