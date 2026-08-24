package com.aimall.mapper;

import com.aimall.entity.OrderItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderItemMapper {

    @Select("SELECT * FROM order_item WHERE order_id=#{orderId}")
    List<OrderItem> findByOrderId(Long orderId);

    @Insert("INSERT INTO order_item(order_id,product_id,product_name,product_image,price,quantity,total_amount) " +
            "VALUES(#{orderId},#{productId},#{productName},#{productImage},#{price},#{quantity},#{totalAmount})")
    int insert(OrderItem item);

    @Select("SELECT * FROM order_item WHERE product_id=#{productId} AND order_id IN (SELECT id FROM orders WHERE user_id=#{userId})")
    List<OrderItem> findUserPurchased(@Param("userId") Long userId, @Param("productId") Long productId);

    @Select("SELECT oi.* FROM order_item oi INNER JOIN orders o ON oi.order_id=o.id WHERE o.user_id=#{userId} ORDER BY oi.id DESC")
    List<OrderItem> findByUserId(Long userId);
}
