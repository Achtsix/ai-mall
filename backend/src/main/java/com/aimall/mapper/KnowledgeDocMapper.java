package com.aimall.mapper;

import com.aimall.entity.KnowledgeDoc;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface KnowledgeDocMapper {

    @Select("SELECT * FROM knowledge_doc WHERE id=#{id}")
    KnowledgeDoc findById(Long id);

    @Select("SELECT * FROM knowledge_doc ORDER BY id DESC")
    List<KnowledgeDoc> findAll();

    @Select("SELECT * FROM knowledge_doc WHERE product_id=#{productId}")
    List<KnowledgeDoc> findByProductId(Long productId);

    @Insert("INSERT INTO knowledge_doc(product_id,title,type,content) VALUES(#{productId},#{title},#{type},#{content})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(KnowledgeDoc doc);

    @Update("UPDATE knowledge_doc SET product_id=#{productId}, title=#{title}, type=#{type}, content=#{content} WHERE id=#{id}")
    int update(KnowledgeDoc doc);

    @Delete("DELETE FROM knowledge_doc WHERE id=#{id}")
    int deleteById(Long id);
}
