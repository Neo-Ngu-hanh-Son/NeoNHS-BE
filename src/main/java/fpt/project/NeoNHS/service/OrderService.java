package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.order.CreateOrderRequest;
import fpt.project.NeoNHS.entity.Order;

public interface OrderService {
    Order createOrder(String userEmail, CreateOrderRequest request);

    void handlePaymentSuccess(long orderCode);
}
