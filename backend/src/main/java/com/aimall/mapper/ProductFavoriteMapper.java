package com.aimall.mapper;

import com.aimall.entity.ProductFavorite;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductFavoriteMapper {

    @Select("SELECT * FROM product_favorite WHERE user_id=#{userId} AND product_id=#{productId}")
    ProductFavorite find(@Param("userId") Long userId, @Param("productId") Long productId);

    @Select("SELECT f.*, p.name AS productName, p.main_image AS productImage, p.price AS price FROM product_favorite f " +
            "LEFT JOIN product p ON f.product_id=p.id WHERE f.user_id=#{userId} ORDER BY f.id DESC")
    List<ProductFavorite> findByUserId(Long userId);

    @Insert("INSERT INTO product_favorite(user_id,product_id) VALUES(#{userId},#{productId})")
    int insert(ProductFavorite favorite);

    @Delete("DELETE FROM product_favorite WHERE user_id=#{userId} AND product_id=#{productId}")
    int delete(@Param("userId") Long userId, @Param("productId") Long productId);
}
