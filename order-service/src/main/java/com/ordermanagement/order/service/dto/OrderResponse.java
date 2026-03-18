package com.ordermanagement.order.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing an order and its associated items")
public class OrderResponse {

    @Schema(description = "Order details")
    private Order order;

    @Schema(description = "List of items included in the order")
    private List<OrderItem> items;
}