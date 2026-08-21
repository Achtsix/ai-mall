package com.aimall.mapper;

import com.aimall.entity.RecommendResult;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RecommendResultMapper {

    @Select("SELECT * FROM recommend_result WHERE id=#{id}")
    RecommendResult findById(Long id);

    @Select("SELECT r.*, p.name AS productName, p.main_image AS productImage FROM recommend_result r " +
            "LEFT JOIN product p ON r.product_id=p.id WHERE r.user_id=#{userId} ORDER BY r.id DESC")
    List<RecommendResult> findByUserId(Long userId);

    @Select("SELECT r.*, p.name AS productName, p.main_image AS productImage FROM recommend_result r " +
            "LEFT JOIN product p ON r.product_id=p.id ORDER BY r.id DESC")
    List<RecommendResult> findAll();

    @Select("SELECT r.*, p.name AS productName, p.main_image AS productImage FROM recommend_result r " +
            "LEFT JOIN product p ON r.product_id=p.id WHERE r.guide_task_id=#{taskId} ORDER BY r.id DESC")
    List<RecommendResult> findByTaskId(Long taskId);

    @Insert("INSERT INTO recommend_result(guide_task_id,run_id,user_id,product_id,reason,price_snapshot,stock_snapshot,discount_snapshot) " +
            "VALUES(#{guideTaskId},#{runId},#{userId},#{productId},#{reason},#{priceSnapshot},#{stockSnapshot},#{discountSnapshot})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RecommendResult result);
}
