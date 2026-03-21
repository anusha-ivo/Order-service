package com.ordermanagement.order.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "Represents an order placed by a customer")
public class Order {

    @Schema(description = "Unique ID of the order", example = "1001")
    private Long orderId;

    @NotNull
    @Schema(description = "ID of the customer who placed the order", example = "101", required = true)
    private Long customerId;

    @NotNull
    @Schema(description = "Current status of the order", example = "CREATED", required = true)
    private String status;

    @NotNull
    @Schema(description = "Total amount of the order", example = "250.75", required = true)
    private BigDecimal totalAmount;

    @NotNull
    @Schema(description = "Currency code for the order (ISO 4217)", example = "USD", required = true)
    private String currency;

    @Schema(description = "Payment ID associated with the order, if any", example = "5001")
    private Long paymentId;

    @NotNull
    @Schema(description = "Shipping address for the order in JSON format",
            example = "{\"street\":\"123 Main St\",\"city\":\"New York\",\"zip\":\"10001\",\"country\":\"USA\"}",
            required = true)
    private String shippingAddress;


}