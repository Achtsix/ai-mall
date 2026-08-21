package com.aimall.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WalletRecharge {
    private Long id;
    private Long userId;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private Integer status;
    private LocalDateTime createTime;
}
