package com.example.orderservice.service;

import com.example.orderservice.client.ProductServiceClient;
import com.example.orderservice.dto.OrderRequest;
import com.example.orderservice.dto.OrderResponse;
import com.example.orderservice.dto.ProductDTO;
import com.example.orderservice.exception.ProductNotFoundException;
import com.example.orderservice.messaging.OrderPublisher;
import com.example.orderservice.model.Order;
import com.example.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @Mock
    private OrderPublisher orderPublisher;

    @InjectMocks
    private OrderServiceImpl orderService;

    private OrderRequest request;
    private ProductDTO product;
    private Order savedOrder;

    @BeforeEach
    void setUp() {
        request = new OrderRequest(1L, 1L, 3);

        product = new ProductDTO();
        product.setProductId(1L);
        product.setName("Laptop");
        product.setUnitPrice(100.0);

        savedOrder = new Order(1L, 1L, "Laptop", 3, 300.0);
        savedOrder.setOrderId(10L);
    }

    @Test
    void testCreateOrder_Success() {
        when(productServiceClient.getProductById(1L)).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(10L, response.getOrderId());
        assertEquals("Laptop", response.getProductName());
        assertEquals(300.0, response.getTotalPrice());
        assertEquals("CREATED", response.getStatus());

        verify(productServiceClient, times(1)).getProductById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderPublisher, times(1)).publishOrder(any(Order.class));
    }

    @Test
    void testCreateOrder_CalculatesTotalPriceCorrectly() {
        when(productServiceClient.getProductById(1L)).thenReturn(product);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponse response = orderService.createOrder(request);

        // 3 qty * 100.0 unit price = 300.0
        assertEquals(300.0, response.getTotalPrice(), 0.01);
    }

    @Test
    void testCreateOrder_ProductNotFound_ThrowsException() {
        when(productServiceClient.getProductById(99L))
                .thenThrow(new ProductNotFoundException("Product not found with id: 99"));

        OrderRequest badRequest = new OrderRequest(1L, 99L, 2);

        assertThrows(ProductNotFoundException.class, () -> orderService.createOrder(badRequest));

        verify(orderRepository, never()).save(any(Order.class));
        verify(orderPublisher, never()).publishOrder(any(Order.class));
    }
}
