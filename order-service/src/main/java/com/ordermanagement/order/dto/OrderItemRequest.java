package com.ordermanagement.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request object representing a single item in an order")
public class OrderItemRequest {

    @NotNull
    @Schema(description = "ID of the product to order", example = "101", required = true)
    private Long productId;

    @NotNull
    @Schema(description = "Quantity of the product to order", example = "2", required = true)
    private Integer quantity;
}