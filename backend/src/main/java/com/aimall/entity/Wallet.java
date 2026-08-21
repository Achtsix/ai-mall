package com.aimall.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Wallet {
    private Long id;
    private Long userId;
    private BigDecimal balance;
    private LocalDateTime updateTime;
}
