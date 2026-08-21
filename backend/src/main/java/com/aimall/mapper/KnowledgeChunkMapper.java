package com.aimall.mapper;

import com.aimall.entity.KnowledgeChunk;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface KnowledgeChunkMapper {

    @Select("SELECT * FROM knowledge_chunk WHERE id=#{id}")
    KnowledgeChunk findById(Long id);

    @Select("SELECT * FROM knowledge_chunk WHERE doc_id=#{docId}")
    List<KnowledgeChunk> findByDocId(Long docId);

    @Select("SELECT * FROM knowledge_chunk ORDER BY id DESC")
    List<KnowledgeChunk> findAllChunks();

    @Insert("INSERT INTO knowledge_chunk(doc_id,content,embedding_json) VALUES(#{docId},#{content},#{embeddingJson})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(KnowledgeChunk chunk);

    @Delete("DELETE FROM knowledge_chunk WHERE doc_id=#{docId}")
    int deleteByDocId(Long docId);
}
