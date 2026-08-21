package com.aimall.mapper;

import com.aimall.entity.HighFreqQuestion;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface HighFreqQuestionMapper {

    @Select("SELECT * FROM high_freq_question ORDER BY count DESC LIMIT #{limit}")
    List<HighFreqQuestion> findTop(@Param("limit") int limit);

    @Select("SELECT * FROM high_freq_question WHERE question=#{question}")
    HighFreqQuestion findByQuestion(String question);

    @Insert("INSERT INTO high_freq_question(question,count) VALUES(#{question},1)")
    int insert(HighFreqQuestion q);

    @Update("UPDATE high_freq_question SET count=count+1, last_ask_time=NOW() WHERE id=#{id}")
    int increaseCount(Long id);
}
