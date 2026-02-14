package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.cart.AddToCartRequest;
import fpt.project.NeoNHS.dto.request.cart.CheckoutRequest;
import fpt.project.NeoNHS.dto.request.cart.UpdateCartItemRequest;
import fpt.project.NeoNHS.dto.response.cart.CartItemResponse;
import fpt.project.NeoNHS.dto.response.cart.CartResponse;
import fpt.project.NeoNHS.dto.response.cart.CheckoutResponse;
import fpt.project.NeoNHS.dto.response.voucher.UserVoucherRespone;
import fpt.project.NeoNHS.entity.Cart;
import fpt.project.NeoNHS.entity.CartItem;
import fpt.project.NeoNHS.entity.TicketCatalog;
import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.entity.UserVoucher;
import fpt.project.NeoNHS.entity.Voucher;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.repository.CartItemRepository;
import fpt.project.NeoNHS.repository.CartRepository;
import fpt.project.NeoNHS.repository.TicketCatalogRepository;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.repository.UserVoucherRepository;
import fpt.project.NeoNHS.service.CartService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final TicketCatalogRepository ticketCatalogRepository;
    private final UserVoucherRepository userVoucherRepository;

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(String userEmail) {
        User user = getUserByEmail(userEmail);
        Cart cart = getOrCreateCart(user);
        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addToCart(String userEmail, AddToCartRequest request) {
        User user = getUserByEmail(userEmail);
        Cart cart = getOrCreateCart(user);
        TicketCatalog ticketCatalog = ticketCatalogRepository.findById(request.getTicketCatalogId())
                .orElseThrow(() -> new BadRequestException("Ticket Catalog not found"));

        if (cart.getCartItems() == null) {
            cart.setCartItems(new ArrayList<>());
        }

        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getTicketCatalog() != null
                        && item.getTicketCatalog().getId().equals(ticketCatalog.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .ticketCatalog(ticketCatalog)
                    .quantity(request.getQuantity())
                    .build();
            cart.getCartItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        cart.setTotalItem(cart.getCartItems().size());
        cartRepository.save(cart);

        // Refund/Reload logic might be needed to get latest state or just map directly
        // Assuming objects are tracked by Hibernate session
        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse updateCartItem(String userEmail, UUID cartItemId, UpdateCartItemRequest request) {
        User user = getUserByEmail(userEmail);
        Cart cart = getOrCreateCart(user);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BadRequestException("Cart item not found"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Item does not belong to user's cart");
        }

        cartItem.setQuantity(request.getQuantity());
        cartItemRepository.save(cartItem);

        return mapToCartResponse(cart);
    }

    @Override
    @Transactional
    public void removeFromCart(String userEmail, UUID cartItemId) {
        User user = getUserByEmail(userEmail);
        Cart cart = getOrCreateCart(user);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new BadRequestException("Cart item not found"));

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Item does not belong to user's cart");
        }

        cart.getCartItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        cart.setTotalItem(cart.getCartItems().size());
        cartRepository.save(cart);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .totalItem(0)
                            .cartItems(new ArrayList<>())
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    private CartResponse mapToCartResponse(Cart cart) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        var items = new ArrayList<CartItemResponse>();

        if (cart.getCartItems() != null) {
            for (CartItem item : cart.getCartItems()) {
                BigDecimal itemPrice = BigDecimal.ZERO;
                String itemName = "";
                UUID ticketId = null;

                if (item.getTicketCatalog() != null) {
                    itemPrice = item.getTicketCatalog().getPrice();
                    itemName = item.getTicketCatalog().getName();
                    ticketId = item.getTicketCatalog().getId();
                }

                BigDecimal subTotal = itemPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                totalPrice = totalPrice.add(subTotal);

                items.add(CartItemResponse.builder()
                        .id(item.getId())
                        .ticketCatalogId(ticketId)
                        .itemName(itemName)
                        .price(itemPrice)
                        .quantity(item.getQuantity())
                        .totalPrice(subTotal)
                        .build());
            }
        }

        return CartResponse.builder()
                .id(cart.getId())
                .totalItems(items.size())
                .totalPrice(totalPrice)
                .items(items)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CheckoutResponse preCheckoutCart(String userEmail, CheckoutRequest request) {
        User user = getUserByEmail(userEmail);

        if (request.getCartItemIds() == null || request.getCartItemIds().isEmpty()) {
            throw new BadRequestException("No items selected for checkout");
        }

        List<CartItem> selectedItems = cartItemRepository.findAllById(request.getCartItemIds());

        // Validate ownership and existence
        if (selectedItems.size() != request.getCartItemIds().size()) {
            throw new BadRequestException("Some cart items not found");
        }

        BigDecimal totalPrice = BigDecimal.ZERO;
        List<CartItemResponse> itemResponses = new ArrayList<>();

        for (CartItem item : selectedItems) {
            if (!item.getCart().getUser().getId().equals(user.getId())) {
                throw new BadRequestException("Item " + item.getId() + " does not belong to user");
            }

            BigDecimal itemPrice = item.getTicketCatalog().getPrice();
            BigDecimal subTotal = itemPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            totalPrice = totalPrice.add(subTotal);

            itemResponses.add(CartItemResponse.builder()
                    .id(item.getId())
                    .ticketCatalogId(item.getTicketCatalog().getId())
                    .itemName(item.getTicketCatalog().getName())
                    .price(itemPrice)
                    .quantity(item.getQuantity())
                    .totalPrice(subTotal)
                    .build());
        }

        // Voucher Logic
        List<UserVoucher> userVouchers = userVoucherRepository.findByUser_IdAndIsUsedFalse(user.getId());
        List<UserVoucherRespone> validVouchers = new ArrayList<>();
        List<UserVoucherRespone> invalidVouchers = new ArrayList<>();

        // Final values to return
        BigDecimal discountValue = BigDecimal.ZERO;
        UserVoucherRespone appliedVoucherResponse = null;

        LocalDateTime now = LocalDateTime.now();

        // 1. First Pass: Identify all valid/invalid vouchers based on BASE total price
        for (UserVoucher uv : userVouchers) {
            Voucher v = uv.getVoucher();
            boolean isValid = true;

            // Check Date
            if ((v.getStartDate() != null && now.isBefore(v.getStartDate())) ||
                    (v.getEndDate() != null && now.isAfter(v.getEndDate()))) {
                isValid = false;
            }

            // Check Usage Limit
            if (v.getUsageLimit() != null && v.getUsageCount() >= v.getUsageLimit()) {
                isValid = false;
            }

            // Check Min Order Value
            if (v.getMinOrderValue() != null && totalPrice.compareTo(v.getMinOrderValue()) < 0) {
                isValid = false;
            }

            UserVoucherRespone response = UserVoucherRespone.builder()
                    .userVoucherId(uv.getId())
                    .code(v.getCode())
                    .description(v.getDescription())
                    .discountValue(v.getDiscountValue())
                    .type(v.getDiscountType())
                    .minOrderValue(v.getMinOrderValue())
                    .build();

            if (isValid) {
                validVouchers.add(response);
            } else {
                invalidVouchers.add(response);
            }
        }

        // 2. Second Pass: If a voucher is requested, try to apply it
        if (request.getVoucherIds() != null && !request.getVoucherIds().isEmpty()) {
            UUID reqId = request.getVoucherIds().get(0);

            // Check if the requested voucher exists in the USER's vouchers
            UserVoucher appliedVoucherEntity = userVouchers.stream()
                    .filter(uv -> uv.getId().equals(reqId))
                    .findFirst()
                    .orElseThrow(() -> new BadRequestException("Voucher not found"));

            // Check if it was deemed valid in step 1
            boolean isValid = validVouchers.stream().anyMatch(v -> v.getUserVoucherId().equals(reqId));

            if (!isValid) {
                throw new BadRequestException("Selected voucher is not applicable for this order");
            }

            Voucher v = appliedVoucherEntity.getVoucher();
            if (v.getDiscountType() == fpt.project.NeoNHS.enums.DiscountType.PERCENT) {
                discountValue = totalPrice.multiply(v.getDiscountValue().divide(BigDecimal.valueOf(100)));
                if (v.getMaxDiscountValue() != null && discountValue.compareTo(v.getMaxDiscountValue()) > 0) {
                    discountValue = v.getMaxDiscountValue();
                }
            } else {
                discountValue = v.getDiscountValue();
            }

            appliedVoucherResponse = validVouchers.stream()
                    .filter(r -> r.getUserVoucherId().equals(reqId))
                    .findFirst().orElse(null);
        }

        BigDecimal finalTotalPrice = totalPrice.subtract(discountValue).max(BigDecimal.ZERO);

        return CheckoutResponse.builder()
                .cartItems(itemResponses)
                .totalPrice(totalPrice)
                .validVouchers(validVouchers)
                .invalidVouchers(invalidVouchers)
                .discountValue(discountValue)
                .finalTotalPrice(finalTotalPrice)
                .transactionDate(LocalDateTime.now())
                .appliedVoucher(appliedVoucherResponse)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserVoucherRespone> getUserVouchers(String userEmail) {
        User user = getUserByEmail(userEmail);
        List<UserVoucher> userVouchers = userVoucherRepository.findByUser_IdAndIsUsedFalse(user.getId());

        List<UserVoucherRespone> voucherResponses = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (UserVoucher uv : userVouchers) {
            Voucher v = uv.getVoucher();
            if ((v.getStartDate() != null && now.isBefore(v.getStartDate())) ||
                    (v.getEndDate() != null && now.isAfter(v.getEndDate()))) {
                continue;
            }
            if (v.getUsageLimit() != null && v.getUsageCount() >= v.getUsageLimit()) {
                continue;
            }

            voucherResponses.add(UserVoucherRespone.builder()
                    .userVoucherId(uv.getId())
                    .code(v.getCode())
                    .description(v.getDescription())
                    .discountValue(v.getDiscountValue())
                    .type(v.getDiscountType())
                    .minOrderValue(v.getMinOrderValue())
                    .build());
        }
        return voucherResponses;
    }
}
