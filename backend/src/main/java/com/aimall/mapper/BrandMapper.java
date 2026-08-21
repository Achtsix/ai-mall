package com.aimall.mapper;

import com.aimall.entity.Brand;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BrandMapper {

    @Select("SELECT * FROM brand ORDER BY id DESC")
    List<Brand> findAll();

    @Select("SELECT * FROM brand WHERE id=#{id}")
    Brand findById(Long id);

    @Insert("INSERT INTO brand(name,logo,description) VALUES(#{name},#{logo},#{description})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Brand brand);

    @Update("UPDATE brand SET name=#{name}, logo=#{logo}, description=#{description} WHERE id=#{id}")
    int update(Brand brand);

    @Delete("DELETE FROM brand WHERE id=#{id}")
    int deleteById(Long id);
}
