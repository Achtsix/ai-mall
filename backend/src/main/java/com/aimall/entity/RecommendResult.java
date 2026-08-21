package com.aimall.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RecommendResult {
    private Long id;
    private Long guideTaskId;
    private Long runId;
    private Long userId;
    private Long productId;
    private String reason;
    private BigDecimal priceSnapshot;
    private Integer stockSnapshot;
    private String discountSnapshot;
    private LocalDateTime createTime;

    private String productName;
    private String productImage;
}
