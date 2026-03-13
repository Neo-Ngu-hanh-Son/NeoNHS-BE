package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.voucher.CreateVoucherRequest;
import fpt.project.NeoNHS.dto.request.voucher.UpdateVoucherRequest;
import fpt.project.NeoNHS.dto.request.voucher.VoucherFilterRequest;
import fpt.project.NeoNHS.dto.response.voucher.UserVoucherRespone;
import fpt.project.NeoNHS.dto.response.voucher.VoucherResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface VoucherService {

    // ===== Admin =====
    VoucherResponse createAdminVoucher(CreateVoucherRequest request);

    Page<VoucherResponse> getAllVouchers(VoucherFilterRequest filter, Pageable pageable);

    VoucherResponse getVoucherById(UUID id);

    VoucherResponse updateVoucher(UUID id, UpdateVoucherRequest request);

    void deleteVoucher(UUID id);

    void hardDeleteVoucher(UUID id);

    VoucherResponse restoreVoucher(UUID id);

    // ===== Vendor =====
    VoucherResponse createVendorVoucher(CreateVoucherRequest request);

    Page<VoucherResponse> getMyVendorVouchers(VoucherFilterRequest filter, Pageable pageable);

    VoucherResponse updateVendorVoucher(UUID id, UpdateVoucherRequest request);

    void deleteVendorVoucher(UUID id);

    void hardDeleteVendorVoucher(UUID id);

    VoucherResponse restoreVendorVoucher(UUID id);

    // ===== Tourist =====
    Page<VoucherResponse> getAvailablePlatformVouchers(Pageable pageable);

    Page<VoucherResponse> getAvailableVendorVouchers(UUID vendorId, Pageable pageable);

    UserVoucherRespone collectVoucher(UUID voucherId);

    Page<UserVoucherRespone> getMyVouchers(Boolean isUsed, Pageable pageable);
}
