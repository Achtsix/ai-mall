package com.aimall.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductFavorite {
    private Long id;
    private Long userId;
    private Long productId;
    private LocalDateTime createTime;

    private String productName;
    private String productImage;
    private java.math.BigDecimal price;
}
