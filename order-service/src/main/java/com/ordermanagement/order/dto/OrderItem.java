package com.ordermanagement.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Represents an item in an order, including a snapshot of product details and pricing")
public class OrderItem {

    @Schema(description = "Unique ID of the order item", example = "5001")
    private Long orderItemId;

    @NotNull
    @Schema(description = "ID of the order this item belongs to", example = "1001", required = true)
    private Long orderId;

    @NotNull
    @Schema(description = "ID of the product for this order item", example = "101", required = true)
    private Long productId;

    @NotNull
    @Schema(description = "Snapshot of the product name at the time of order", example = "Wireless Mouse", required = true)
    private String productName;

    @NotNull
    @Schema(description = "Snapshot of the product unit price at the time of order", example = "25.50", required = true)
    private BigDecimal unitPriceSnapshot;

    @NotNull
    @Schema(description = "Quantity of this product in the order", example = "2", required = true)
    private Integer quantity;

    @NotNull
    @Schema(description = "Total price for this item line (unit price × quantity)", example = "51.00", required = true)
    private BigDecimal lineTotal;
}