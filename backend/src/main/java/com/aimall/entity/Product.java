package com.aimall.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Product {
    private Long id;
    private Long categoryId;
    private Long brandId;
    private String name;
    private String subtitle;
    private String mainImage;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private Integer sales;
    private Integer status;
    private String detailHtml;
    private String paramsJson;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 联表展示用
    private String categoryName;
    private String brandName;
}
