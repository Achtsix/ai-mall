package com.aimall.service;

import com.aimall.common.BusinessException;
import com.aimall.entity.*;
import com.aimall.mapper.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final CategoryMapper categoryMapper;
    private final BrandMapper brandMapper;
    private final AfterSaleRuleMapper afterSaleRuleMapper;
    private final KnowledgeDocMapper knowledgeDocMapper;
    private final FunctionToolMapper functionToolMapper;
    private final FunctionCallLogMapper functionCallLogMapper;
    private final AgentRunMapper agentRunMapper;
    private final AgentStepMapper agentStepMapper;
    private final PromptTemplateMapper promptTemplateMapper;
    private final ModelConfigMapper modelConfigMapper;
    private final GuideTaskMapper guideTaskMapper;
    private final RecommendResultMapper recommendResultMapper;
    private final EvaluationAnalysisMapper evaluationAnalysisMapper;
    private final OperationReportMapper operationReportMapper;
    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;

    public AdminService(CategoryMapper categoryMapper,
                        BrandMapper brandMapper,
                        AfterSaleRuleMapper afterSaleRuleMapper,
                        KnowledgeDocMapper knowledgeDocMapper,
                        FunctionToolMapper functionToolMapper,
                        FunctionCallLogMapper functionCallLogMapper,
                        AgentRunMapper agentRunMapper,
                        AgentStepMapper agentStepMapper,
                        PromptTemplateMapper promptTemplateMapper,
                        ModelConfigMapper modelConfigMapper,
                        GuideTaskMapper guideTaskMapper,
                        RecommendResultMapper recommendResultMapper,
                        EvaluationAnalysisMapper evaluationAnalysisMapper,
                        OperationReportMapper operationReportMapper,
                        ReviewMapper reviewMapper,
                        OrderMapper orderMapper) {
        this.categoryMapper = categoryMapper;
        this.brandMapper = brandMapper;
        this.afterSaleRuleMapper = afterSaleRuleMapper;
        this.knowledgeDocMapper = knowledgeDocMapper;
        this.functionToolMapper = functionToolMapper;
        this.functionCallLogMapper = functionCallLogMapper;
        this.agentRunMapper = agentRunMapper;
        this.agentStepMapper = agentStepMapper;
        this.promptTemplateMapper = promptTemplateMapper;
        this.modelConfigMapper = modelConfigMapper;
        this.guideTaskMapper = guideTaskMapper;
        this.recommendResultMapper = recommendResultMapper;
        this.evaluationAnalysisMapper = evaluationAnalysisMapper;
        this.operationReportMapper = operationReportMapper;
        this.reviewMapper = reviewMapper;
        this.orderMapper = orderMapper;
    }

    // 分类
    public List<Category> listCategories() { return categoryMapper.findAll(); }
    public Category saveCategory(Category category) {
        if (category.getId() == null) categoryMapper.insert(category); else categoryMapper.update(category);
        return category;
    }
    public void deleteCategory(Long id) { categoryMapper.deleteById(id); }

    // 品牌
    public List<Brand> listBrands() { return brandMapper.findAll(); }
    public Brand saveBrand(Brand brand) {
        if (brand.getId() == null) brandMapper.insert(brand); else brandMapper.update(brand);
        return brand;
    }
    public void deleteBrand(Long id) { brandMapper.deleteById(id); }

    // 售后规则
    public List<AfterSaleRule> listAfterSaleRules() { return afterSaleRuleMapper.findAll(); }
    public AfterSaleRule saveAfterSaleRule(AfterSaleRule rule) {
        if (rule.getId() == null) afterSaleRuleMapper.insert(rule); else afterSaleRuleMapper.update(rule);
        return rule;
    }
    public void deleteAfterSaleRule(Long id) { afterSaleRuleMapper.deleteById(id); }

    // 知识库
    public List<KnowledgeDoc> listKnowledgeDocs() { return knowledgeDocMapper.findAll(); }
    public KnowledgeDoc saveKnowledgeDoc(KnowledgeDoc doc) {
        if (doc.getId() == null) knowledgeDocMapper.insert(doc); else knowledgeDocMapper.update(doc);
        return doc;
    }
    public void deleteKnowledgeDoc(Long id) { knowledgeDocMapper.deleteById(id); }

    // Function Tool
    public List<FunctionTool> listFunctionTools() { return functionToolMapper.findAll(); }
    public FunctionTool saveFunctionTool(FunctionTool tool) {
        if (tool.getId() == null) functionToolMapper.insert(tool); else functionToolMapper.update(tool);
        return tool;
    }
    public void deleteFunctionTool(Long id) { functionToolMapper.deleteById(id); }
    public List<FunctionCallLog> listFunctionCallLogs() { return functionCallLogMapper.findAll(); }

    // Agent
    public List<AgentRun> listAgentRuns() { return agentRunMapper.findAll(); }
    public AgentRun agentRunDetail(Long id) {
        AgentRun run = agentRunMapper.findById(id);
        if (run == null) throw new BusinessException(404, "记录不存在");
        run.setSteps(agentStepMapper.findByRunId(id));
        return run;
    }

    // Prompt / 模型
    public List<PromptTemplate> listPromptTemplates() { return promptTemplateMapper.findAll(); }
    public PromptTemplate savePromptTemplate(PromptTemplate t) {
        if (t.getId() == null) promptTemplateMapper.insert(t); else promptTemplateMapper.update(t);
        return t;
    }
    public List<ModelConfig> listModelConfigs() {
        List<ModelConfig> configs = modelConfigMapper.findAll();
        configs.forEach(config -> config.setApiKey(null));
        return configs;
    }
    public ModelConfig saveModelConfig(ModelConfig c) {
        c.setApiKey(null);
        if (c.getId() == null) modelConfigMapper.insert(c); else modelConfigMapper.update(c);
        return c;
    }

    // 导购任务/推荐
    public List<GuideTask> listGuideTasks() { return guideTaskMapper.findAll(); }
    public List<RecommendResult> listRecommendResults() { return recommendResultMapper.findAll(); }
    public List<RecommendResult> listRecommendByTask(Long taskId) { return recommendResultMapper.findByTaskId(taskId); }

    // 评价分析与运营报告
    public List<EvaluationAnalysis> listEvaluationAnalysis() { return evaluationAnalysisMapper.findAll(); }
    public List<OperationReport> listOperationReports() { return operationReportMapper.findAll(); }

    public List<Order> listOrders() { return orderMapper.findAll(); }
    public List<Review> listReviews() { return reviewMapper.findAll(); }
}
