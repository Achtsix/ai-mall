package com.aimall.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OperationReport {
    private Long id;
    private String title;
    private String content;
    private String period;
    private LocalDateTime createTime;
}
