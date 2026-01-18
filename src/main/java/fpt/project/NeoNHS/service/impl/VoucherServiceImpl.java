package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.VoucherRepository;
import fpt.project.NeoNHS.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
}
