package com.ordermanagement.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object representing product details")
public class ProductResponse {

    @Schema(description = "Unique ID of the product", example = "101")
    private Long productId;

    @Schema(description = "Stock Keeping Unit (SKU) of the product", example = "SKU12345")
    private String stock_keeping_unit;

    @Schema(description = "Name of the product", example = "Wireless Mouse")
    private String name;

    @Schema(description = "Description of the product", example = "Ergonomic wireless mouse with USB receiver")
    private String description;

    @Schema(description = "Price of the product", example = "25.50")
    private BigDecimal price;

    @Schema(description = "Currency code for the price (ISO 4217)", example = "USD")
    private String currency;

    @Schema(description = "Current status of the product", example = "ACTIVE")
    private String status;

    @Schema(description = "Available stock quantity for the product", example = "100")
    private Integer stock;
}