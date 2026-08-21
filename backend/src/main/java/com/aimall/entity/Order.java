package com.aimall.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Order {
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private Integer status;
    private String addressSnapshot;
    private LocalDateTime payTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private List<OrderItem> items;
}
