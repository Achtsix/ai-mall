package com.aimall.mapper;

import com.aimall.entity.AgentStep;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AgentStepMapper {

    @Select("SELECT * FROM agent_step WHERE run_id=#{runId} ORDER BY seq ASC, id ASC")
    List<AgentStep> findByRunId(Long runId);

    @Insert("INSERT INTO agent_step(run_id,seq,tool_name,input_json,output_json,status,cost_ms) VALUES(#{runId},#{seq},#{toolName},#{inputJson},#{outputJson},#{status},#{costMs})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AgentStep step);
}
