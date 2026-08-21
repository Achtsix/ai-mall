package com.aimall.mapper;

import com.aimall.entity.AgentRun;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AgentRunMapper {

    @Select("SELECT * FROM agent_run WHERE id=#{id}")
    AgentRun findById(Long id);

    @Select("SELECT * FROM agent_run WHERE user_id=#{userId} ORDER BY id DESC")
    List<AgentRun> findByUserId(Long userId);

    @Select("SELECT * FROM agent_run ORDER BY id DESC")
    List<AgentRun> findAll();

    @Insert("INSERT INTO agent_run(user_id,question,model,status,answer,started_at,finished_at) VALUES(#{userId},#{question},#{model},#{status},#{answer},#{startedAt},#{finishedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AgentRun run);

    @Update("UPDATE agent_run SET status=#{status}, answer=#{answer}, finished_at=#{finishedAt} WHERE id=#{id}")
    int update(AgentRun run);
}
