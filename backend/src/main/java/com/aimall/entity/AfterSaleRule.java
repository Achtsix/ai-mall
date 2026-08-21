package com.aimall.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AfterSaleRule {
    private Long id;
    private String title;
    private String content;
    private String category;
    private String keywords;
    private Integer priority;
    private LocalDateTime createTime;
}
