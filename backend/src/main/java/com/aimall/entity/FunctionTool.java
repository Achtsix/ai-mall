package com.aimall.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FunctionTool {
    private Long id;
    private String name;
    private String description;
    private String url;
    private String method;
    private String requestSchema;
    private String responseSchema;
    private Integer enabled;
    private LocalDateTime createTime;
}
