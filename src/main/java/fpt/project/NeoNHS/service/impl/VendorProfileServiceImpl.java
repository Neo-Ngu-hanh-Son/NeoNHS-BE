package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.VendorProfileRepository;
import fpt.project.NeoNHS.service.VendorProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VendorProfileServiceImpl implements VendorProfileService {

    private final VendorProfileRepository vendorProfileRepository;
}
