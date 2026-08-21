package com.aimall.mapper;

import com.aimall.entity.FunctionTool;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FunctionToolMapper {

    @Select("SELECT * FROM function_tool WHERE id=#{id}")
    FunctionTool findById(Long id);

    @Select("SELECT * FROM function_tool ORDER BY id DESC")
    List<FunctionTool> findAll();

    @Select("SELECT * FROM function_tool WHERE enabled=1 ORDER BY id ASC")
    List<FunctionTool> findEnabled();

    @Insert("INSERT INTO function_tool(name,description,url,method,request_schema,response_schema,enabled) VALUES(#{name},#{description},#{url},#{method},#{requestSchema},#{responseSchema},#{enabled})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FunctionTool tool);

    @Update("UPDATE function_tool SET name=#{name}, description=#{description}, url=#{url}, method=#{method}, request_schema=#{requestSchema}, response_schema=#{responseSchema}, enabled=#{enabled} WHERE id=#{id}")
    int update(FunctionTool tool);

    @Delete("DELETE FROM function_tool WHERE id=#{id}")
    int deleteById(Long id);
}
