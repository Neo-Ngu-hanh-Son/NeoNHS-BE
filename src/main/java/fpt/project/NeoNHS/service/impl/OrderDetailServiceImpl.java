package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.OrderDetailRepository;
import fpt.project.NeoNHS.service.OrderDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderDetailServiceImpl implements OrderDetailService {

    private final OrderDetailRepository orderDetailRepository;
}
