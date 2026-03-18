package com.ordermanagement.order.service.controller;

import com.ordermanagement.order.service.dto.CreateOrderRequest;
import com.ordermanagement.order.service.dto.Order;
import com.ordermanagement.order.service.dto.OrderItem;
import com.ordermanagement.order.service.dto.OrderResponse;
import com.ordermanagement.order.service.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Order Controller", description = "Handles order lifecycle operations")
@RequestMapping("/orders")

public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @Operation(summary = "Create Order", description = "Creates a new order with items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Customer/Product not found"),
            @ApiResponse(responseCode = "502", description = "External service failure")
    })

    @PostMapping
    public ResponseEntity<Order> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {//remove this exception

        Long orderId = service.createOrder(request);

        Order order = service.getOrder(orderId); // fetch full order

        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @Operation(summary = "Confirm Order", description = "Confirms order and triggers payment + inventory deduction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order confirmed"),
            @ApiResponse(responseCode = "400", description = "Invalid order state"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "502", description = "Payment/Inventory service failure")
    })
    @PostMapping("/{id}/confirm")
    public ResponseEntity<Order> confirmOrder(@PathVariable Long id) {

        service.confirmOrder(id);

        Order order = service.getOrder(id);

        return ResponseEntity.ok(order);
    }



    @Operation(summary = "Cancel Order", description = "Cancels order and triggers refund + inventory restore")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order cancelled"),
            @ApiResponse(responseCode = "400", description = "Invalid order state"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "502", description = "External service failure")
    })

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable Long id) {

        service.cancelOrder(id);

        Order order = service.getOrder(id);

        return ResponseEntity.ok(order);
    }

    @Operation(summary = "Get Order", description = "Fetch order details with items")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order fetched successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long id) {

        Order order = service.getOrder(id);
        List<OrderItem> items = service.getOrderItems(id);

        OrderResponse response = new OrderResponse(order, items);

        return ResponseEntity.ok(response);
    }
}