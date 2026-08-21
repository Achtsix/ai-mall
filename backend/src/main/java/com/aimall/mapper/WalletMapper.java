package com.aimall.mapper;

import com.aimall.entity.Wallet;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;

@Mapper
public interface WalletMapper {

    @Select("SELECT * FROM wallet WHERE user_id=#{userId}")
    Wallet findByUserId(Long userId);

    @Insert("INSERT INTO wallet(user_id,balance) VALUES(#{userId},#{balance})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Wallet wallet);

    @Update("UPDATE wallet SET balance=#{balance} WHERE id=#{id}")
    int updateBalance(Wallet wallet);

    @Update("UPDATE wallet SET balance = balance + #{amount} WHERE user_id=#{userId}")
    int increase(@Param("userId") Long userId, @Param("amount") BigDecimal amount);

    @Update("UPDATE wallet SET balance = balance - #{amount} WHERE user_id=#{userId} AND balance >= #{amount}")
    int decrease(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
}
