package com.aimall.mapper;

import com.aimall.entity.WalletRecharge;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface WalletRechargeMapper {

    @Select("SELECT * FROM wallet_recharge WHERE user_id=#{userId} ORDER BY id DESC")
    List<WalletRecharge> findByUserId(Long userId);

    @Insert("INSERT INTO wallet_recharge(user_id,amount,balance_after,status) VALUES(#{userId},#{amount},#{balanceAfter},#{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(WalletRecharge recharge);
}
