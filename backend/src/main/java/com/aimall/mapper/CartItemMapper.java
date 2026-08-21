package com.aimall.mapper;

import com.aimall.entity.CartItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CartItemMapper {

    @Select("SELECT c.*, p.name AS productName, p.main_image AS productImage, p.price AS price, p.stock AS stock " +
            "FROM cart_item c LEFT JOIN product p ON c.product_id=p.id WHERE c.user_id=#{userId} ORDER BY c.id DESC")
    List<CartItem> findByUserId(Long userId);

    @Select("SELECT * FROM cart_item WHERE user_id=#{userId} AND product_id=#{productId}")
    CartItem find(@Param("userId") Long userId, @Param("productId") Long productId);

    @Insert("INSERT INTO cart_item(user_id,product_id,quantity,checked) VALUES(#{userId},#{productId},#{quantity},#{checked})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CartItem cartItem);

    @Update("UPDATE cart_item SET quantity=#{quantity}, checked=#{checked} WHERE id=#{id} AND user_id=#{userId}")
    int update(CartItem cartItem);

    @Update("UPDATE cart_item SET quantity = quantity + #{delta} WHERE id=#{id} AND user_id=#{userId} AND quantity + #{delta} > 0")
    int changeQuantity(@Param("id") Long id, @Param("userId") Long userId, @Param("delta") int delta);

    @Update("UPDATE cart_item SET checked=#{checked} WHERE user_id=#{userId}")
    int updateAllChecked(@Param("userId") Long userId, @Param("checked") int checked);

    @Delete("DELETE FROM cart_item WHERE id=#{id} AND user_id=#{userId}")
    int delete(@Param("id") Long id, @Param("userId") Long userId);

    @Delete("DELETE FROM cart_item WHERE user_id=#{userId} AND checked=1")
    int deleteChecked(Long userId);
}
