package com.aimall.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Category {
    private Long id;
    private Long parentId;
    private String name;
    private Integer sort;
    private LocalDateTime createTime;
}
