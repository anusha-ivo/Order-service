package com.ordermanagement.order.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long productId;
    private String stock_keeping_unit;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private String status;
    private Integer stock;

}
