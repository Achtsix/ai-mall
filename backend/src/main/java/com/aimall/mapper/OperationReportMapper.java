package com.aimall.mapper;

import com.aimall.entity.OperationReport;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OperationReportMapper {

    @Select("SELECT * FROM operation_report WHERE id=#{id}")
    OperationReport findById(Long id);

    @Select("SELECT * FROM operation_report ORDER BY id DESC")
    List<OperationReport> findAll();

    @Insert("INSERT INTO operation_report(title,content,period) VALUES(#{title},#{content},#{period})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OperationReport report);

    @Delete("DELETE FROM operation_report WHERE id=#{id}")
    int deleteById(Long id);
}
