package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.cart.AddToCartRequest;
import fpt.project.NeoNHS.dto.request.cart.CheckoutRequest;
import fpt.project.NeoNHS.dto.request.cart.UpdateCartItemRequest;
import fpt.project.NeoNHS.dto.response.cart.CartItemResponse;
import fpt.project.NeoNHS.dto.response.cart.CartResponse;
import fpt.project.NeoNHS.dto.response.cart.CheckoutResponse;
import fpt.project.NeoNHS.dto.response.voucher.UserVoucherRespone;
import fpt.project.NeoNHS.dto.response.voucher.VoucherClassificationResult;
import fpt.project.NeoNHS.entity.Cart;
import fpt.project.NeoNHS.entity.CartItem;
import fpt.project.NeoNHS.entity.TicketCatalog;
import fpt.project.NeoNHS.entity.User;
import fpt.project.NeoNHS.entity.UserVoucher;
import fpt.project.NeoNHS.entity.Voucher;
import fpt.project.NeoNHS.entity.WorkshopSession;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.repository.CartItemRepository;
import fpt.project.NeoNHS.repository.CartRepository;
import fpt.project.NeoNHS.repository.TicketCatalogRepository;
import fpt.project.NeoNHS.repository.UserRepository;
import fpt.project.NeoNHS.repository.UserVoucherRepository;
import fpt.project.NeoNHS.repository.WorkshopSessionRepository;
import fpt.project.NeoNHS.service.CartService;
import fpt.project.NeoNHS.enums.VoucherType;
import fpt.project.NeoNHS.service.VoucherService;
import java.time.LocalDateTime;
import fpt.project.NeoNHS.entity.Event;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import fpt.project.NeoNHS.service.validator.AvailabilityValidator;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final TicketCatalogRepository ticketCatalogRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final WorkshopSessionRepository workshopSessionRepository;
    private final AvailabilityValidator avai;
    private final VoucherService voucherService;

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

        if (request.getTicketCatalogId() == null && request.getWorkshopSessionId() == null) {
            throw new BadRequestException("Either Ticket Catalog ID or Workshop Session ID must be provided");
        }

        if (cart.getCartItems() == null) {
            cart.setCartItems(new ArrayList<>());
        }

        int totalQuantityToCheck = request.getQuantity();

        if (request.getTicketCatalogId() != null) {
            TicketCatalog ticketCatalog = ticketCatalogRepository.findById(request.getTicketCatalogId())
                    .orElseThrow(() -> new BadRequestException("Ticket Catalog not found"));

            Optional<CartItem> existingItem = cart.getCartItems().stream()
                    .filter(item -> item.getTicketCatalog() != null
                            && item.getTicketCatalog().getId().equals(ticketCatalog.getId()))
                    .findFirst();

            if (existingItem.isPresent()) {
                totalQuantityToCheck += existingItem.get().getQuantity();
            }

            avai.validateTicketAvailability(ticketCatalog, totalQuantityToCheck);

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
        } else {
            WorkshopSession workshopSession = workshopSessionRepository.findById(request.getWorkshopSessionId())
                    .orElseThrow(() -> new BadRequestException("Workshop Session not found"));

            Optional<CartItem> existingItem = cart.getCartItems().stream()
                    .filter(item -> item.getWorkshopSession() != null
                            && item.getWorkshopSession().getId().equals(workshopSession.getId()))
                    .findFirst();

            if (existingItem.isPresent()) {
                totalQuantityToCheck += existingItem.get().getQuantity();
            }

            avai.validateWorkshopAvailability(workshopSession, totalQuantityToCheck);

            if (existingItem.isPresent()) {
                CartItem item = existingItem.get();
                item.setQuantity(item.getQuantity() + request.getQuantity());
                cartItemRepository.save(item);
            } else {
                CartItem newItem = CartItem.builder()
                        .cart(cart)
                        .workshopSession(workshopSession)
                        .quantity(request.getQuantity())
                        .build();
                cart.getCartItems().add(newItem);
                cartItemRepository.save(newItem);
            }
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

        if (cartItem.getTicketCatalog() != null) {
            avai.validateTicketAvailability(cartItem.getTicketCatalog(), request.getQuantity());
        } else if (cartItem.getWorkshopSession() != null) {
            avai.validateWorkshopAvailability(cartItem.getWorkshopSession(), request.getQuantity());
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
                UUID eventId = null;
                String eventName = null;
                UUID workshopSessionId = null;
                UUID workshopTemplateId = null;
                String workshopName = null;

                if (item.getTicketCatalog() != null) {
                    itemPrice = item.getTicketCatalog().getPrice();
                    itemName = item.getTicketCatalog().getName();
                    ticketId = item.getTicketCatalog().getId();

                    // Extract event info
                    Event event = item.getTicketCatalog().getEvent();
                    if (event != null) {
                        eventId = event.getId();
                        eventName = event.getName();
                    }
                }

                // Extract workshop info
                if (item.getWorkshopSession() != null) {
                    workshopSessionId = item.getWorkshopSession().getId();
                    if (item.getWorkshopSession().getWorkshopTemplate() != null) {
                        workshopName = item.getWorkshopSession().getWorkshopTemplate().getName();
                        workshopTemplateId = item.getWorkshopSession().getWorkshopTemplate().getId();
                    }
                    if (itemPrice.compareTo(BigDecimal.ZERO) == 0 && item.getWorkshopSession().getPrice() != null) {
                        itemPrice = item.getWorkshopSession().getPrice();
                        itemName = workshopName != null ? workshopName : itemName;
                    }
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
                        .eventId(eventId)
                        .eventName(eventName)
                        .workshopSessionId(workshopSessionId)
                        .workshopTemplateId(workshopTemplateId)
                        .workshopName(workshopName)
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

            // Re-validate availability at pre-checkout
            if (item.getTicketCatalog() != null) {
                avai.validateTicketAvailability(item.getTicketCatalog(), item.getQuantity());
            } else if (item.getWorkshopSession() != null) {
                avai.validateWorkshopAvailability(item.getWorkshopSession(), item.getQuantity());
            }

            BigDecimal itemPrice = item.getTicketCatalog() != null
                    ? item.getTicketCatalog().getPrice()
                    : (item.getWorkshopSession() != null && item.getWorkshopSession().getPrice() != null
                            ? item.getWorkshopSession().getPrice()
                            : BigDecimal.ZERO);
            BigDecimal subTotal = itemPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            totalPrice = totalPrice.add(subTotal);

            // Extract event info
            UUID eventId = null;
            String eventName = null;
            if (item.getTicketCatalog() != null && item.getTicketCatalog().getEvent() != null) {
                eventId = item.getTicketCatalog().getEvent().getId();
                eventName = item.getTicketCatalog().getEvent().getName();
            }

            UUID workshopTemplateId2 = null;
            UUID workshopSessionId = null;
            String workshopName = null;
            if (item.getWorkshopSession() != null) {
                workshopSessionId = item.getWorkshopSession().getId();
                if (item.getWorkshopSession().getWorkshopTemplate() != null) {
                    workshopName = item.getWorkshopSession().getWorkshopTemplate().getName();
                    workshopTemplateId2 = item.getWorkshopSession().getWorkshopTemplate().getId();
                }
            }

            String itemName = item.getTicketCatalog() != null
                    ? item.getTicketCatalog().getName()
                    : (workshopName != null ? workshopName : "Unknown item");

            itemResponses.add(CartItemResponse.builder()
                    .id(item.getId())
                    .ticketCatalogId(item.getTicketCatalog() != null ? item.getTicketCatalog().getId() : null)
                    .itemName(itemName)
                    .price(itemPrice)
                    .quantity(item.getQuantity())
                    .totalPrice(subTotal)
                    .eventId(eventId)
                    .eventName(eventName)
                    .workshopSessionId(workshopSessionId)
                    .workshopTemplateId(workshopTemplateId2)
                    .workshopName(workshopName)
                    .build());
        }

        // ── Voucher Logic (delegated to VoucherService) ──
        VoucherClassificationResult classification =
                voucherService.classifyVouchersForCart(user, selectedItems, totalPrice);

        BigDecimal discountValue = BigDecimal.ZERO;
        UserVoucherRespone appliedVoucherResponse = null;

        if (request.getVoucherIds() != null && !request.getVoucherIds().isEmpty()) {
            UUID reqId = request.getVoucherIds().get(0);
            discountValue = voucherService.applyVoucher(reqId, selectedItems, totalPrice, classification);
            appliedVoucherResponse = classification.getValidVouchers().stream()
                    .filter(v -> v.getUserVoucherId().equals(reqId))
                    .findFirst().orElse(null);
        }

        BigDecimal finalTotalPrice = totalPrice.subtract(discountValue).max(BigDecimal.ZERO);

        return CheckoutResponse.builder()
                .cartItems(itemResponses)
                .totalPrice(totalPrice)
                .validVouchers(classification.getValidVouchers())
                .invalidVouchers(classification.getInvalidVouchers())
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
            
            // Only return DISCOUNT vouchers for cart
            if (v.getVoucherType() != VoucherType.DISCOUNT) {
                continue;
            }

            if ((v.getStartDate() != null && now.isBefore(v.getStartDate())) ||
                    (v.getEndDate() != null && now.isAfter(v.getEndDate()))) {
                continue;
            }
            if (v.getUsageLimit() != null && v.getUsageCount() >= v.getUsageLimit()) {
                continue;
            }

            voucherResponses.add(UserVoucherRespone.fromEntity(uv));
        }
        return voucherResponses;
    }

}
