package com.aimall.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EvaluationAnalysis {
    private Long id;
    private Long productId;
    private String summary;
    private String positiveKeywords;
    private String negativeReasons;
    private String afterSaleRisks;
    private String missingInfo;
    private String suggestions;
    private LocalDateTime createTime;
}
