package com.aimall.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String username;
    @JsonIgnore
    private String password;
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    private String role;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
