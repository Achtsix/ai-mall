package com.aimall.mapper;

import com.aimall.entity.FunctionCallLog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FunctionCallLogMapper {

    @Select("SELECT * FROM function_call_log WHERE id=#{id}")
    FunctionCallLog findById(Long id);

    @Select("SELECT * FROM function_call_log ORDER BY id DESC")
    List<FunctionCallLog> findAll();

    @Select("SELECT * FROM function_call_log WHERE run_id=#{runId} ORDER BY id ASC")
    List<FunctionCallLog> findByRunId(Long runId);

    @Insert("INSERT INTO function_call_log(run_id,step_id,tool_name,input_json,output_json,status,cost_ms) VALUES(#{runId},#{stepId},#{toolName},#{inputJson},#{outputJson},#{status},#{costMs})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(FunctionCallLog log);
}
