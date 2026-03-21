package com.ordermanagement.order.entity;


import lombok.Data;

import java.math.BigDecimal;

@Data


public class OrderItemEntity {

    private Long orderItemId;
    private Long orderId;
    private Long productId;
    private String productNameSnapshot;
    private BigDecimal unitPriceSnapshot;
    private Integer quantity;
    private BigDecimal lineTotal;

    // getters & setters
}
