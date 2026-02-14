package fpt.project.NeoNHS.service;

import fpt.project.NeoNHS.dto.request.order.CreateOrderRequest;
import fpt.project.NeoNHS.entity.Order;
import fpt.project.NeoNHS.entity.Transaction;

public interface OrderService {
    Order createOrder(String userEmail, CreateOrderRequest request);

    void handlePaymentSuccess(long orderCode);
}
