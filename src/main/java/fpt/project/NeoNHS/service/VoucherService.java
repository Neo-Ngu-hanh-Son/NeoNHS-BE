package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.voucher.CreateVoucherRequest;
import fpt.project.NeoNHS.dto.request.voucher.UpdateVoucherRequest;
import fpt.project.NeoNHS.dto.request.voucher.VoucherFilterRequest;
import fpt.project.NeoNHS.dto.response.voucher.UserVoucherRespone;
import fpt.project.NeoNHS.dto.response.voucher.VoucherClassificationResult;
import fpt.project.NeoNHS.dto.response.voucher.VoucherResponse;
import fpt.project.NeoNHS.entity.CartItem;
import fpt.project.NeoNHS.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
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

    VoucherResponse getVendorVoucherById(UUID id);

    VoucherResponse updateVendorVoucher(UUID id, UpdateVoucherRequest request);

    void deleteVendorVoucher(UUID id);

    void hardDeleteVendorVoucher(UUID id);

    VoucherResponse restoreVendorVoucher(UUID id);

    // ===== Tourist =====
    Page<VoucherResponse> getAvailablePlatformVouchers(VoucherFilterRequest filter, Pageable pageable);

    Page<VoucherResponse> getAvailableVendorVouchers(UUID vendorId, VoucherFilterRequest filter, Pageable pageable);

    Page<VoucherResponse> getAvailableAllVendorVouchers(VoucherFilterRequest filter, Pageable pageable);

    UserVoucherRespone collectVoucher(UUID voucherId);

    Page<UserVoucherRespone> getMyVouchers(Boolean isUsed, Pageable pageable);

    UserVoucherRespone redeemVoucher(UUID userVoucherId);
    // ===== Cart / Pre-Checkout // Tourist =====
    VoucherClassificationResult classifyVouchersForCart(
            User user,
            List<CartItem> cartItems,
            BigDecimal totalPrice
    );

    BigDecimal applyVoucher(
            UUID userVoucherId,
            List<CartItem> cartItems,
            BigDecimal totalPrice,
            VoucherClassificationResult classification
    );
}
