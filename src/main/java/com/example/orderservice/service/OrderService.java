package com.example.orderservice.service;

import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;

/**
 * Service interface for handling order-related operations.
 */
public interface OrderService {
    OrderResponse createOrder(OrderRequest request);
}
