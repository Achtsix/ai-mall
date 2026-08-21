package com.aimall.mapper;

import com.aimall.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CategoryMapper {

    @Select("SELECT * FROM category ORDER BY sort ASC, id ASC")
    List<Category> findAll();

    @Select("SELECT * FROM category WHERE id=#{id}")
    Category findById(Long id);

    @Insert("INSERT INTO category(parent_id,name,sort) VALUES(#{parentId},#{name},#{sort})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

    @Update("UPDATE category SET parent_id=#{parentId}, name=#{name}, sort=#{sort} WHERE id=#{id}")
    int update(Category category);

    @Delete("DELETE FROM category WHERE id=#{id}")
    int deleteById(Long id);
}
