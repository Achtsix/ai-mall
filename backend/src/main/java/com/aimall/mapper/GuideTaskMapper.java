package com.aimall.mapper;

import com.aimall.entity.GuideTask;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface GuideTaskMapper {

    @Select("SELECT * FROM guide_task WHERE id=#{id}")
    GuideTask findById(Long id);

    @Select("SELECT * FROM guide_task WHERE user_id=#{userId} ORDER BY id DESC")
    List<GuideTask> findByUserId(Long userId);

    @Select("SELECT * FROM guide_task ORDER BY id DESC")
    List<GuideTask> findAll();

    @Insert("INSERT INTO guide_task(user_id,question,status,run_id) VALUES(#{userId},#{question},#{status},#{runId})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(GuideTask task);

    @Update("UPDATE guide_task SET status=#{status}, run_id=#{runId} WHERE id=#{id}")
    int update(GuideTask task);
}
