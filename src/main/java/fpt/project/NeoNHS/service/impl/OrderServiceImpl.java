package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.OrderRepository;
import fpt.project.NeoNHS.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
}
