package fpt.project.NeoNHS.controller;

import fpt.project.NeoNHS.dto.request.cart.AddToCartRequest;
import fpt.project.NeoNHS.dto.request.cart.CheckoutRequest;
import fpt.project.NeoNHS.dto.request.cart.UpdateCartItemRequest;
import fpt.project.NeoNHS.dto.response.ApiResponse;
import fpt.project.NeoNHS.dto.response.cart.CartResponse;
import fpt.project.NeoNHS.dto.response.cart.CheckoutResponse;
import fpt.project.NeoNHS.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import fpt.project.NeoNHS.dto.response.voucher.UserVoucherRespone;
import java.security.Principal;
import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Principal principal) {
        CartResponse response = cartService.getCart(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Get cart successfully", response));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            Principal principal,
            @Valid @RequestBody AddToCartRequest request) {
        CartResponse response = cartService.addToCart(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Added to cart successfully", response));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            Principal principal,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        CartResponse response = cartService.updateCartItem(principal.getName(), itemId, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Updated cart item successfully", response));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<String>> removeFromCart(
            Principal principal,
            @PathVariable UUID itemId) {
        cartService.removeFromCart(principal.getName(), itemId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Removed item from cart successfully", "Removed"));
    }

    @PostMapping("/pre-checkout")
    public ResponseEntity<ApiResponse<CheckoutResponse>> preCheckoutCart(Principal principal,
            @Valid @RequestBody CheckoutRequest request) {
        CheckoutResponse response = cartService.preCheckoutCart(principal.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Pre-checked out successfully", response));
    }

    @GetMapping("/vouchers")
    public ResponseEntity<ApiResponse<List<UserVoucherRespone>>> getUserVouchers(Principal principal) {
        List<UserVoucherRespone> vouchers = cartService.getUserVouchers(principal.getName());
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Get user vouchers successfully", vouchers));
    }
}
