package com.aimall.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KnowledgeChunk {
    private Long id;
    private Long docId;
    private String content;
    private String embeddingJson;
    private LocalDateTime createTime;
}
