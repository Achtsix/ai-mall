package com.aimall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.aimall.mapper")
public class AimallApplication {
    public static void main(String[] args) {
        SpringApplication.run(AimallApplication.class, args);
    }
}
