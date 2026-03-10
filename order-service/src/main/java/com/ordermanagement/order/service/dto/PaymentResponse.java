package com.ordermanagement.order.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;


    @Data

    @NoArgsConstructor
    @AllArgsConstructor
    public class PaymentResponse {
        private Long paymentId;
        private Long orderId;
        private BigDecimal amount;
        private String currency;
        private String method;
        private String status;
        private Instant createdAt;
        private Instant updatedAt;
    }

