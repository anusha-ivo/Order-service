package com.ordermanagement.order.service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class OrderItem {

    private Long orderItemId;

    @NotNull
    private Long orderId;

    @NotNull
    private Long productId;

    @NotNull
    private String productNameSnapshot;

    @NotNull
    private BigDecimal unitPriceSnapshot;

    @NotNull
    private Integer quantity;

    @NotNull
    private BigDecimal lineTotal;


}
