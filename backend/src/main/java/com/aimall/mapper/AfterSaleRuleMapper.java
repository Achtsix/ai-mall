package com.aimall.mapper;

import com.aimall.entity.AfterSaleRule;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AfterSaleRuleMapper {

    @Select("SELECT * FROM after_sale_rule ORDER BY priority ASC, id DESC")
    List<AfterSaleRule> findAll();

    @Select("SELECT * FROM after_sale_rule WHERE id=#{id}")
    AfterSaleRule findById(Long id);

    @Select("SELECT * FROM after_sale_rule WHERE keywords LIKE CONCAT('%',#{keyword},'%') OR title LIKE CONCAT('%',#{keyword},'%') OR content LIKE CONCAT('%',#{keyword},'%') ORDER BY priority ASC")
    List<AfterSaleRule> search(String keyword);

    @Insert("INSERT INTO after_sale_rule(title,content,category,keywords,priority) VALUES(#{title},#{content},#{category},#{keywords},#{priority})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AfterSaleRule rule);

    @Update("UPDATE after_sale_rule SET title=#{title}, content=#{content}, category=#{category}, keywords=#{keywords}, priority=#{priority} WHERE id=#{id}")
    int update(AfterSaleRule rule);

    @Delete("DELETE FROM after_sale_rule WHERE id=#{id}")
    int deleteById(Long id);
}
