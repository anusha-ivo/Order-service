package com.ordermanagement.order.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request object to modify product inventory (deduct or restore stock)")
public class InventoryRequest {

    @Schema(description = "ID of the product whose inventory is being modified", example = "101", required = true)
    private Long productId;

    @Schema(description = "Quantity of stock to deduct or restore", example = "5", required = true)
    private Integer quantity;
}