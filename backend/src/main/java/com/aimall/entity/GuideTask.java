package com.aimall.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GuideTask {
    private Long id;
    private Long userId;
    private String question;
    private String status;
    private Long runId;
    private LocalDateTime createTime;
}
