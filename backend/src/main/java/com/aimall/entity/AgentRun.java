package com.aimall.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AgentRun {
    private Long id;
    private Long userId;
    private String question;
    private String model;
    private String status;
    private String answer;
    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createTime;

    private List<AgentStep> steps;
}
