package com.ordermanagement.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Schema(description = "Request object to create a new order")
public class CreateOrderRequest {

    @NotNull
    @Schema(description = "ID of the customer placing the order", example = "101", required = true)
    private Long customerId;



    @NotNull
    @Schema(description = "Shipping address for the order as key-value pairs",
            example = "{\"street\":\"123 Main St\", \"city\":\"New York\", \"zip\":\"10001\", \"country\":\"USA\"}",
            required = true)
    private Map<String, Object> shippingAddress;

    @NotEmpty
    @Valid
    @Schema(description = "List of items to be ordered", required = true)
    private List<OrderItemRequest> items;
}