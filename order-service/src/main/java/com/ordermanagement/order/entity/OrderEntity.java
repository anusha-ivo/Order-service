package com.ordermanagement.order.entity;


import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class OrderEntity {

    private Long orderId;
    private Long customerId;
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    private Long paymentId;
    private String shippingAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
