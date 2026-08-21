package com.aimall.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Brand {
    private Long id;
    private String name;
    private String logo;
    private String description;
    private LocalDateTime createTime;
}
