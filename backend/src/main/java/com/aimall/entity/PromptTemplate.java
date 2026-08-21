package com.aimall.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PromptTemplate {
    private Long id;
    private String name;
    private String type;
    private String content;
    private Integer enabled;
    private LocalDateTime updateTime;
}
