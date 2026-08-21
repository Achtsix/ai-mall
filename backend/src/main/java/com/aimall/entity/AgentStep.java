package com.aimall.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AgentStep {
    private Long id;
    private Long runId;
    private Integer seq;
    private String toolName;
    private String inputJson;
    private String outputJson;
    private String status;
    private Long costMs;
    private LocalDateTime createTime;
}
