package com.aimall.entity;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ModelConfig {
    private Long id;
    private String name;
    private String provider;
    private String baseUrl;
    @JsonIgnore
    private String apiKey;
    private String model;
    private BigDecimal temperature;
    private Integer maxTokens;
    private Integer enabled;
    private LocalDateTime updateTime;
}
