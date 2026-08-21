package com.aimall.mapper;

import com.aimall.entity.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderMapper {

    @Select("SELECT * FROM orders WHERE id=#{id}")
    Order findById(Long id);

    @Select("SELECT * FROM orders WHERE order_no=#{orderNo}")
    Order findByOrderNo(String orderNo);

    @Select("SELECT * FROM orders WHERE user_id=#{userId} ORDER BY id DESC")
    List<Order> findByUserId(Long userId);

    @Select("SELECT * FROM orders ORDER BY id DESC")
    List<Order> findAll();

    @Insert("INSERT INTO orders(order_no,user_id,total_amount,pay_amount,status,address_snapshot,pay_time) " +
            "VALUES(#{orderNo},#{userId},#{totalAmount},#{payAmount},#{status},#{addressSnapshot},#{payTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Update("UPDATE orders SET status=#{status}, pay_time=#{payTime} WHERE id=#{id}")
    int updateStatus(Order order);
}
