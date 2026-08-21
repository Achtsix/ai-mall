package com.aimall.mapper;

import com.aimall.entity.EvaluationAnalysis;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface EvaluationAnalysisMapper {

    @Select("SELECT * FROM evaluation_analysis WHERE id=#{id}")
    EvaluationAnalysis findById(Long id);

    @Select("SELECT * FROM evaluation_analysis ORDER BY id DESC")
    List<EvaluationAnalysis> findAll();

    @Select("SELECT * FROM evaluation_analysis WHERE product_id=#{productId} ORDER BY id DESC LIMIT 1")
    EvaluationAnalysis findByProductId(Long productId);

    @Insert("INSERT INTO evaluation_analysis(product_id,summary,positive_keywords,negative_reasons,after_sale_risks,missing_info,suggestions) " +
            "VALUES(#{productId},#{summary},#{positiveKeywords},#{negativeReasons},#{afterSaleRisks},#{missingInfo},#{suggestions})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(EvaluationAnalysis analysis);

    @Update("UPDATE evaluation_analysis SET summary=#{summary}, positive_keywords=#{positiveKeywords}, negative_reasons=#{negativeReasons}, after_sale_risks=#{afterSaleRisks}, missing_info=#{missingInfo}, suggestions=#{suggestions} WHERE id=#{id}")
    int update(EvaluationAnalysis analysis);
}
