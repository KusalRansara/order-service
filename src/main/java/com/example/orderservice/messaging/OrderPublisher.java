package com.example.orderservice.messaging;

import com.example.orderservice.model.Order;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class OrderPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${order.rabbitmq.exchange}")
    private String exchange;

    @Value("${order.rabbitmq.routingkey}")
    private String routingKey;

    public OrderPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishOrder(Order order) {
        Map<String, Object> message = new HashMap<>();
        message.put("orderId", order.getOrderId());
        message.put("customerId", order.getCustomerId());
        message.put("productId", order.getProductId());
        message.put("productName", order.getProductName());
        message.put("totalPrice", order.getTotalPrice());

        rabbitTemplate.convertAndSend(exchange, routingKey, message);
        System.out.println("Order event published to RabbitMQ: orderId=" + order.getOrderId());
    }
}
