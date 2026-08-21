package com.aimall.mapper;

import com.aimall.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM sys_user WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT * FROM sys_user WHERE username = #{username}")
    User findByUsername(String username);

    @Select("SELECT * FROM sys_user WHERE role = 'USER'")
    List<User> findAllUsers();

    @Insert("INSERT INTO sys_user(username,password,nickname,phone,email,role,status) VALUES(#{username},#{password},#{nickname},#{phone},#{email},#{role},#{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE sys_user SET nickname=#{nickname}, phone=#{phone}, email=#{email}, avatar=#{avatar}, status=#{status} WHERE id=#{id}")
    int update(User user);

    @Update("UPDATE sys_user SET password=#{password} WHERE id=#{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    @Delete("DELETE FROM sys_user WHERE id=#{id}")
    int deleteById(Long id);
}
