package com.aimall.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HighFreqQuestion {
    private Long id;
    private String question;
    private Integer count;
    private LocalDateTime lastAskTime;
}
