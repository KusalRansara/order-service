package com.example.orderservice.service;

import com.example.orderservice.client.ProductServiceClient;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.dto.ProductDTO;
import com.example.orderservice.messaging.OrderPublisher;
import com.example.orderservice.model.Order;
import com.example.orderservice.repository.OrderRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final OrderPublisher orderPublisher;

    public OrderServiceImpl(OrderRepository orderRepository,
                            ProductServiceClient productServiceClient,
                            OrderPublisher orderPublisher) {
        this.orderRepository = orderRepository;
        this.productServiceClient = productServiceClient;
        this.orderPublisher = orderPublisher;
    }

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        // Step 1: Fetch product details from Product Service via REST
        ProductDTO product = productServiceClient.getProductById(request.getProductId());

        // Step 2: Calculate total price
        double totalPrice = product.getUnitPrice() * request.getQuantity();

        // Step 3: Save order to database
        Order order = new Order(
                request.getCustomerId(),
                request.getProductId(),
                product.getName(),
                request.getQuantity(),
                totalPrice
        );
        Order savedOrder = orderRepository.save(order);

        // Step 4: Publish order event to RabbitMQ
        orderPublisher.publishOrder(savedOrder);

        return mapToResponse(savedOrder);
    }

    private OrderResponse mapToResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setCustomerId(order.getCustomerId());
        response.setProductId(order.getProductId());
        response.setProductName(order.getProductName());
        response.setQuantity(order.getQuantity());
        response.setTotalPrice(order.getTotalPrice());
        response.setStatus(order.getStatus());
        return response;
    }
}
