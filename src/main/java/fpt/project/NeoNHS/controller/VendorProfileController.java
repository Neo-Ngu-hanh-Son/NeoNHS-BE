package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.service.VendorProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorProfileController {

    private final VendorProfileService vendorProfileService;
}
