package com.aimall.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProductImage {
    private Long id;
    private Long productId;
    private String url;
    private Integer sort;
    private LocalDateTime createTime;
}
