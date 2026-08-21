package com.aimall.mapper;

import com.aimall.entity.ModelConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ModelConfigMapper {

    @Select("SELECT * FROM model_config WHERE id=#{id}")
    ModelConfig findById(Long id);

    @Select("SELECT * FROM model_config ORDER BY id DESC")
    List<ModelConfig> findAll();

    @Select("SELECT * FROM model_config WHERE enabled=1 ORDER BY id DESC LIMIT 1")
    ModelConfig findEnabled();

    @Insert("INSERT INTO model_config(name,provider,base_url,api_key,model,temperature,max_tokens,enabled) VALUES(#{name},#{provider},#{baseUrl},NULL,#{model},#{temperature},#{maxTokens},#{enabled})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ModelConfig config);

    @Update("UPDATE model_config SET name=#{name}, provider=#{provider}, base_url=#{baseUrl}, api_key=NULL, model=#{model}, temperature=#{temperature}, max_tokens=#{maxTokens}, enabled=#{enabled} WHERE id=#{id}")
    int update(ModelConfig config);

    @Update("UPDATE model_config SET api_key=NULL WHERE api_key IS NOT NULL")
    int clearStoredApiKeys();

    @Delete("DELETE FROM model_config WHERE id=#{id}")
    int deleteById(Long id);
}
