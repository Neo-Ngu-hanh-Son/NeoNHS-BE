package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.dto.request.order.CreateOrderRequest;
import fpt.project.NeoNHS.entity.*;
import fpt.project.NeoNHS.enums.*;
import fpt.project.NeoNHS.exception.BadRequestException;
import fpt.project.NeoNHS.repository.*;
import fpt.project.NeoNHS.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final TransactionRepository transactionRepository;
    private final TicketRepository ticketRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final CartRepository cartRepository;

    @Override
    @Transactional
    public Order createOrder(String userEmail, CreateOrderRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (request.getCartItemIds() == null || request.getCartItemIds().isEmpty()) {
            throw new BadRequestException("No items selected");
        }

        List<CartItem> cartItems = cartItemRepository.findAllById(request.getCartItemIds());
        if (cartItems.size() != request.getCartItemIds().size()) {
            throw new BadRequestException("Some items not found");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            if (!item.getCart().getUser().getId().equals(user.getId())) {
                throw new BadRequestException("Item " + item.getId() + " does not belong to user");
            }
            BigDecimal price = item.getTicketCatalog().getPrice();
            totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        Voucher primaryVoucher = null;
        List<UUID> usedUserVoucherIds = new ArrayList<>();

        // Handle list of vouchers
        if (request.getVoucherIds() != null && !request.getVoucherIds().isEmpty()) {
            List<UserVoucher> userVouchers = userVoucherRepository.findAllById(request.getVoucherIds());
            if (userVouchers.size() != request.getVoucherIds().size()) {
                throw new BadRequestException("Some vouchers not found");
            }

            LocalDateTime now = LocalDateTime.now();

            for (UserVoucher userVoucher : userVouchers) {
                if (!userVoucher.getUser().getId().equals(user.getId())) {
                    throw new BadRequestException("Voucher does not belong to user");
                }
                if (userVoucher.getIsUsed() != null && userVoucher.getIsUsed()) {
                    throw new BadRequestException("Voucher already used");
                }

                Voucher voucher = userVoucher.getVoucher();
                // Validate voucher
                if ((voucher.getStartDate() != null && now.isBefore(voucher.getStartDate())) ||
                        (voucher.getEndDate() != null && now.isAfter(voucher.getEndDate()))) {
                    throw new BadRequestException("Voucher " + voucher.getCode() + " is not valid at this time");
                }
                if (voucher.getUsageLimit() != null && voucher.getUsageCount() >= voucher.getUsageLimit()) {
                    throw new BadRequestException("Voucher " + voucher.getCode() + " usage limit reached");
                }
                if (voucher.getMinOrderValue() != null && totalAmount.compareTo(voucher.getMinOrderValue()) < 0) {
                    throw new BadRequestException("Order amount not sufficient for voucher " + voucher.getCode());
                }

                BigDecimal currentDiscount = BigDecimal.ZERO;
                if (voucher.getDiscountType() == DiscountType.PERCENT) {
                    currentDiscount = totalAmount.multiply(voucher.getDiscountValue()).divide(BigDecimal.valueOf(100));
                    if (voucher.getMaxDiscountValue() != null
                            && currentDiscount.compareTo(voucher.getMaxDiscountValue()) > 0) {
                        currentDiscount = voucher.getMaxDiscountValue();
                    }
                } else {
                    currentDiscount = voucher.getDiscountValue();
                }

                discountAmount = discountAmount.add(currentDiscount);
                usedUserVoucherIds.add(userVoucher.getId());

                if (primaryVoucher == null) {
                    primaryVoucher = voucher;
                }
            }

            if (discountAmount.compareTo(totalAmount) > 0) {
                discountAmount = totalAmount;
            }
        }

        BigDecimal finalAmount = totalAmount.subtract(discountAmount);

        Order order = Order.builder()
                .user(user)
                .voucher(primaryVoucher)
                .totalAmount(totalAmount)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                // .status(OrderStatus.PENDING) // Deleted
                .build();

        order = orderRepository.save(order);

        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartItem item : cartItems) {
            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .ticketCatalog(item.getTicketCatalog())
                    .workshopSession(item.getWorkshopSession())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getTicketCatalog().getPrice())
                    .build();
            orderDetails.add(detail);
        }
        orderDetailRepository.saveAll(orderDetails);
        order.setOrderDetails(orderDetails);

        long orderCode = Long.parseLong(System.currentTimeMillis() + "" + (int) (Math.random() * 10));

        // Encode vouchers in description
        String description = "Payment for order " + order.getId();
        if (!usedUserVoucherIds.isEmpty()) {
            // Simply join IDs with comma
            String encodedIds = usedUserVoucherIds.stream().map(UUID::toString).collect(Collectors.joining(","));
            description += " | Vouchers: " + encodedIds;
        }

        Transaction transaction = Transaction.builder()
                .order(order)
                .amount(finalAmount)
                .paymentGateway("PAYOS_" + orderCode)
                // .paymentGatewayTransactionId(orderCode) // Deleted
                .status(TransactionStatus.PENDING)
                .description(description)
                .currency("VND")
                .build();

        transactionRepository.save(transaction);
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);
        order.setTransactions(transactions);

        return order;
    }

    @Override
    @Transactional
    public void handlePaymentSuccess(long orderCode) {
        Transaction transaction = transactionRepository.findByPaymentGateway("PAYOS_" + orderCode)
                .orElseThrow(() -> new BadRequestException("Transaction not found"));

        if (transaction.getStatus() == TransactionStatus.SUCCESS) {
            return;
        }

        Order order = transaction.getOrder();
        // order.setStatus(OrderStatus.PAID); // Deleted
        transaction.setStatus(TransactionStatus.SUCCESS);

        orderRepository.save(order);
        transactionRepository.save(transaction);

        // Decode Vouchers from Description to mark as used
        String desc = transaction.getDescription();
        if (desc != null && desc.contains("| Vouchers: ")) {
            try {
                String vouchersPart = desc.split("\\| Vouchers: ")[1];
                if (vouchersPart != null && !vouchersPart.isEmpty()) {
                    List<UUID> voucherIds = Arrays.stream(vouchersPart.split(","))
                            .map(String::trim)
                            .map(UUID::fromString)
                            .collect(Collectors.toList());

                    for (UUID uvId : voucherIds) {
                        UserVoucher uv = userVoucherRepository.findById(uvId).orElse(null);
                        if (uv != null && !uv.getIsUsed()) {
                            uv.setIsUsed(true);
                            uv.getVoucher().setUsageCount(uv.getVoucher().getUsageCount() + 1);
                            userVoucherRepository.save(uv);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to parse used vouchers: " + e.getMessage());
            }
        }
        // Fallback for primary voucher if not in description (legacy/safety)
        else if (order.getVoucher() != null) {
            Voucher v = order.getVoucher();
            // Try to find one unused user voucher to burn?
            UserVoucher uv = userVoucherRepository.findByUser_IdAndVoucher_Id(order.getUser().getId(), v.getId())
                    .stream().filter(u -> !u.getIsUsed()).findFirst().orElse(null);
            if (uv != null) {
                uv.setIsUsed(true);
                v.setUsageCount(v.getUsageCount() + 1);
                userVoucherRepository.save(uv);
            }
        }

        for (OrderDetail detail : order.getOrderDetails()) {
            for (int i = 0; i < detail.getQuantity(); i++) {
                TicketType type = TicketType.ENTRANCE;
                if (detail.getWorkshopSession() != null) {
                    type = TicketType.WORKSHOP;
                } else if (detail.getTicketCatalog().getEvent() != null) {
                    type = TicketType.EVENT;
                }

                Ticket ticket = Ticket.builder()
                        .ticketCatalog(detail.getTicketCatalog())
                        .orderDetail(detail)
                        .workshopSession(detail.getWorkshopSession())
                        .status(TicketStatus.ACTIVE)
                        .ticketType(type)
                        .ticketCode(generateTicketCode())
                        .qrCode(UUID.randomUUID().toString())
                        .issueDate(LocalDateTime.now())
                        .build();
                ticketRepository.save(ticket);
            }
        }

        Cart cart = cartRepository.findByUser(order.getUser()).orElse(null);
        if (cart != null && cart.getCartItems() != null) {
            List<CartItem> itemsToRemove = new ArrayList<>();
            for (CartItem ci : cart.getCartItems()) {
                boolean purchased = order.getOrderDetails().stream()
                        .anyMatch(od -> od.getTicketCatalog().getId().equals(ci.getTicketCatalog().getId()));
                if (purchased) {
                    itemsToRemove.add(ci);
                }
            }

            cart.getCartItems().removeAll(itemsToRemove);
            cartItemRepository.deleteAll(itemsToRemove);
            cart.setTotalItem(cart.getCartItems().size());
            cartRepository.save(cart);
        }
    }

    private String generateTicketCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
