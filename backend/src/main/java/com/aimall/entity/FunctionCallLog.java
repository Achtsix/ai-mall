package com.aimall.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FunctionCallLog {
    private Long id;
    private Long runId;
    private Long stepId;
    private String toolName;
    private String inputJson;
    private String outputJson;
    private String status;
    private Long costMs;
    private LocalDateTime createTime;
}
