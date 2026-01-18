package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.CartItemRepository;
import fpt.project.NeoNHS.service.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
}
