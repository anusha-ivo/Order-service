package com.ordermanagement.order.service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class Order {

    private Long orderId;

    @NotNull
    private Long customerId;

    @NotNull
    private String status;

    @NotNull
    private BigDecimal totalAmount;

    @NotNull
    private String currency;

    private Long paymentId;

    @NotNull
    private String shippingAddress;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
