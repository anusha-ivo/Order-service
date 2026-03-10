package com.ordermanagement.order.service.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.Map;
@Data
    public class CreateOrderRequest {

        @NotNull
        private Long customerId;

        @NotNull
        private String currency;

        @NotNull
        private Map<String, Object> shippingAddress;

        @NotEmpty
        @Valid
        private List<OrderItemRequest> items;


    }

