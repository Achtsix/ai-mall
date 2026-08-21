package com.aimall.mapper;

import com.aimall.entity.Address;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AddressMapper {

    @Select("SELECT * FROM user_address WHERE id=#{id} AND user_id=#{userId}")
    Address findByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT * FROM user_address WHERE user_id=#{userId} ORDER BY is_default DESC, id DESC")
    List<Address> findByUserId(Long userId);

    @Insert("INSERT INTO user_address(user_id,receiver_name,receiver_phone,province,city,district,detail,is_default) VALUES(#{userId},#{receiverName},#{receiverPhone},#{province},#{city},#{district},#{detail},#{isDefault})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Address address);

    @Update("UPDATE user_address SET receiver_name=#{receiverName},receiver_phone=#{receiverPhone},province=#{province},city=#{city},district=#{district},detail=#{detail},is_default=#{isDefault} WHERE id=#{id} AND user_id=#{userId}")
    int update(Address address);

    @Update("UPDATE user_address SET is_default=0 WHERE user_id=#{userId}")
    int clearDefault(Long userId);

    @Delete("DELETE FROM user_address WHERE id=#{id} AND user_id=#{userId}")
    int delete(@Param("id") Long id, @Param("userId") Long userId);
}
