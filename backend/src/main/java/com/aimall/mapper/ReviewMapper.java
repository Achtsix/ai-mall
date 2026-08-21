package com.aimall.mapper;

import com.aimall.entity.Review;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ReviewMapper {

    @Select("SELECT r.*, u.username, u.nickname FROM review r LEFT JOIN sys_user u ON r.user_id=u.id WHERE r.id=#{id}")
    Review findById(Long id);

    @Select("SELECT r.*, u.username, u.nickname FROM review r LEFT JOIN sys_user u ON r.user_id=u.id WHERE r.product_id=#{productId} ORDER BY r.id DESC")
    List<Review> findByProductId(Long productId);

    @Select("SELECT r.*, u.username, u.nickname FROM review r LEFT JOIN sys_user u ON r.user_id=u.id ORDER BY r.id DESC")
    List<Review> findAll();

    @Insert("INSERT INTO review(user_id,product_id,order_id,rating,content,images) VALUES(#{userId},#{productId},#{orderId},#{rating},#{content},#{images})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Review review);

    @Update("UPDATE review SET reply=#{reply} WHERE id=#{id}")
    int updateReply(Review review);

    @Delete("DELETE FROM review WHERE id=#{id}")
    int deleteById(Long id);
}
