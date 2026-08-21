package com.aimall.mapper;

import com.aimall.entity.Product;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ProductMapper {

    @Select("SELECT p.*, c.name AS categoryName, b.name AS brandName FROM product p " +
            "LEFT JOIN category c ON p.category_id = c.id " +
            "LEFT JOIN brand b ON p.brand_id = b.id WHERE p.id=#{id}")
    Product findById(Long id);

    @Select("<script>" +
            "SELECT p.*, c.name AS categoryName, b.name AS brandName FROM product p " +
            "LEFT JOIN category c ON p.category_id = c.id " +
            "LEFT JOIN brand b ON p.brand_id = b.id " +
            "WHERE p.status=1 " +
            // A parent category should also include products assigned to its direct children.
            "<if test='categoryId != null'> AND (p.category_id=#{categoryId} OR c.parent_id=#{categoryId})</if>" +
            "<if test='brandId != null'> AND p.brand_id=#{brandId}</if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (p.name LIKE CONCAT('%',#{keyword},'%') OR p.subtitle LIKE CONCAT('%',#{keyword},'%') OR p.detail_html LIKE CONCAT('%',#{keyword},'%') OR p.params_json LIKE CONCAT('%',#{keyword},'%') OR c.name LIKE CONCAT('%',#{keyword},'%') OR b.name LIKE CONCAT('%',#{keyword},'%'))</if>" +
            "<if test='minPrice != null'> AND p.price >= #{minPrice}</if>" +
            "<if test='maxPrice != null'> AND p.price &lt;= #{maxPrice}</if>" +
            " ORDER BY p.sales DESC, p.id DESC" +
            "</script>")
    List<Product> search(@Param("categoryId") Long categoryId,
                         @Param("brandId") Long brandId,
                         @Param("keyword") String keyword,
                         @Param("minPrice") java.math.BigDecimal minPrice,
                         @Param("maxPrice") java.math.BigDecimal maxPrice);

    @Select("SELECT * FROM product WHERE status=1 ORDER BY sales DESC LIMIT #{limit}")
    List<Product> findHot(@Param("limit") int limit);

    @Insert("INSERT INTO product(category_id,brand_id,name,subtitle,main_image,price,original_price,stock,sales,status,detail_html,params_json) " +
            "VALUES(#{categoryId},#{brandId},#{name},#{subtitle},#{mainImage},#{price},#{originalPrice},#{stock},#{sales},#{status},#{detailHtml},#{paramsJson})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    @Update("UPDATE product SET category_id=#{categoryId}, brand_id=#{brandId}, name=#{name}, subtitle=#{subtitle}, main_image=#{mainImage}, " +
            "price=#{price}, original_price=#{originalPrice}, stock=#{stock}, status=#{status}, detail_html=#{detailHtml}, params_json=#{paramsJson} WHERE id=#{id}")
    int update(Product product);

    @Update("UPDATE product SET stock = stock - #{quantity} WHERE id=#{id} AND stock >= #{quantity}")
    int deductStock(@Param("id") Long id, @Param("quantity") int quantity);

    @Update("UPDATE product SET sales = sales + #{quantity} WHERE id=#{id}")
    int increaseSales(@Param("id") Long id, @Param("quantity") int quantity);

    @Delete("DELETE FROM product WHERE id=#{id}")
    int deleteById(Long id);
}
