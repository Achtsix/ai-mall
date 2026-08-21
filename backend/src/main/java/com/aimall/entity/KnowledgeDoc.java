package com.aimall.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KnowledgeDoc {
    private Long id;
    private Long productId;
    private String title;
    private String type;
    private String content;
    private LocalDateTime createTime;
}
