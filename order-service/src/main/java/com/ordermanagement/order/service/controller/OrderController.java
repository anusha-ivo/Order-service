package com.ordermanagement.order.service.controller;

import com.ordermanagement.order.service.dto.CreateOrderRequest;
import com.ordermanagement.order.service.dto.Order;
import com.ordermanagement.order.service.dto.OrderItem;
import com.ordermanagement.order.service.dto.OrderResponse;
import com.ordermanagement.order.service.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }


    @PostMapping
    public ResponseEntity<Order> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {//remove this exception

        Long orderId = service.createOrder(request);

        Order order = service.getOrder(orderId); // fetch full order

        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }


    @PostMapping("/{id}/confirm")
    public ResponseEntity<Order> confirmOrder(@PathVariable Long id) {

        service.confirmOrder(id);

        Order order = service.getOrder(id);

        return ResponseEntity.ok(order);
    }

    


    @PostMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {

        service.cancelOrder(id);

        Order order = service.getOrder(id);

        return ResponseEntity.ok(order);
    }


    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {

        Order order = service.getOrder(id);
        List<OrderItem> items = service.getOrderItems(id);

        OrderResponse response = new OrderResponse(order, items);

        return ResponseEntity.ok(response);
    }
}