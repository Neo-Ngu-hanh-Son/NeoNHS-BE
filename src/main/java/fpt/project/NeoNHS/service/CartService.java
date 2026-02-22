package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.cart.AddToCartRequest;
import fpt.project.NeoNHS.dto.request.cart.CheckoutRequest;
import fpt.project.NeoNHS.dto.request.cart.UpdateCartItemRequest;
import fpt.project.NeoNHS.dto.response.cart.CartResponse;
import fpt.project.NeoNHS.dto.response.cart.CheckoutResponse;
import fpt.project.NeoNHS.dto.response.voucher.UserVoucherRespone;
import java.util.List;
import java.util.UUID;

public interface CartService {
    CartResponse getCart(String userEmail);

    CartResponse addToCart(String userEmail, AddToCartRequest request);

    CartResponse updateCartItem(String userEmail, UUID cartItemId, UpdateCartItemRequest request);

    void removeFromCart(String userEmail, UUID cartItemId);

    CheckoutResponse preCheckoutCart(String userEmail, CheckoutRequest request);

    List<UserVoucherRespone> getUserVouchers(String userEmail);

}
