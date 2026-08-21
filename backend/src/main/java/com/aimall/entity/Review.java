package com.aimall.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Review {
    private Long id;
    private Long userId;
    private Long productId;
    private Long orderId;
    private Integer rating;
    private String content;
    private String images;
    private String reply;
    private LocalDateTime createTime;

    private String username;
    private String nickname;
}
