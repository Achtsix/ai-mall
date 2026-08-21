package com.aimall.mapper;

import com.aimall.entity.PromptTemplate;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PromptTemplateMapper {

    @Select("SELECT * FROM prompt_template WHERE id=#{id}")
    PromptTemplate findById(Long id);

    @Select("SELECT * FROM prompt_template ORDER BY id DESC")
    List<PromptTemplate> findAll();

    @Select("SELECT * FROM prompt_template WHERE type=#{type} AND enabled=1 ORDER BY id DESC LIMIT 1")
    PromptTemplate findLatestByType(String type);

    @Insert("INSERT INTO prompt_template(name,type,content,enabled) VALUES(#{name},#{type},#{content},#{enabled})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PromptTemplate template);

    @Update("UPDATE prompt_template SET name=#{name}, type=#{type}, content=#{content}, enabled=#{enabled} WHERE id=#{id}")
    int update(PromptTemplate template);

    @Delete("DELETE FROM prompt_template WHERE id=#{id}")
    int deleteById(Long id);
}
