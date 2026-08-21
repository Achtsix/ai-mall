package com.aimall.ai;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aimall.entity.*;
import com.aimall.mapper.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AgentEngine {

    private final DeepSeekClient deepSeekClient;
    private final FunctionToolRegistry functionToolRegistry;
    private final AgentRunMapper agentRunMapper;
    private final AgentStepMapper agentStepMapper;
    private final FunctionCallLogMapper functionCallLogMapper;
    private final PromptTemplateMapper promptTemplateMapper;
    private final GuideTaskMapper guideTaskMapper;
    private final RecommendResultMapper recommendResultMapper;
    private final ProductMapper productMapper;

    @Value("${aimall.agent.max-steps:8}")
    private int maxSteps;

    public AgentEngine(DeepSeekClient deepSeekClient,
                       FunctionToolRegistry functionToolRegistry,
                       AgentRunMapper agentRunMapper,
                       AgentStepMapper agentStepMapper,
                       FunctionCallLogMapper functionCallLogMapper,
                       PromptTemplateMapper promptTemplateMapper,
                       GuideTaskMapper guideTaskMapper,
                       RecommendResultMapper recommendResultMapper,
                       ProductMapper productMapper) {
        this.deepSeekClient = deepSeekClient;
        this.functionToolRegistry = functionToolRegistry;
        this.agentRunMapper = agentRunMapper;
        this.agentStepMapper = agentStepMapper;
        this.functionCallLogMapper = functionCallLogMapper;
        this.promptTemplateMapper = promptTemplateMapper;
        this.guideTaskMapper = guideTaskMapper;
        this.recommendResultMapper = recommendResultMapper;
        this.productMapper = productMapper;
    }

    public AgentRun run(Long userId, String question, Long guideTaskId) {
        return run(userId, question, guideTaskId, List.of());
    }

    public String getActiveModel() {
        return deepSeekClient.getActiveModel();
    }

    public boolean isConfigured() {
        return deepSeekClient.isConfigured();
    }

    @SuppressWarnings("unchecked")
    public AgentRun run(Long userId, String question, Long guideTaskId, List<Map<String, Object>> history) {
        AgentRun run = new AgentRun();
        run.setUserId(userId);
        run.setQuestion(question);
        run.setModel(deepSeekClient.getActiveModel());
        run.setStatus("RUNNING");
        run.setStartedAt(LocalDateTime.now());
        agentRunMapper.insert(run);

        if (guideTaskId != null) {
            GuideTask task = guideTaskMapper.findById(guideTaskId);
            if (task != null) {
                task.setStatus("PROCESSING");
                task.setRunId(run.getId());
                guideTaskMapper.update(task);
            }
        }

        try {
            List<Map<String, Object>> messages = new ArrayList<>();
            PromptTemplate guidePrompt = promptTemplateMapper.findLatestByType("GUIDE");
            String systemPrompt = guidePrompt != null ? guidePrompt.getContent() : "你是一个电商导购 Agent，必须使用工具获取真实数据，禁止编造价格库存。";
            Map<String, Object> sys = new HashMap<>();
            sys.put("role", "system");
            sys.put("content", systemPrompt);
            messages.add(sys);

            if (history != null) {
                for (Map<String, Object> previous : history) {
                    String role = String.valueOf(previous.getOrDefault("role", ""));
                    Object content = previous.get("content");
                    if (("user".equals(role) || "assistant".equals(role)) && content != null && !content.toString().isBlank()) {
                        Map<String, Object> message = new HashMap<>();
                        message.put("role", role);
                        message.put("content", content.toString());
                        messages.add(message);
                    }
                }
            }

            Map<String, Object> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", question);
            messages.add(userMsg);

            List<Map<String, Object>> tools = functionToolRegistry.buildToolDefinitions();
            String answer = "";

            for (int step = 1; step <= maxSteps; step++) {
                Map<String, Object> resp = deepSeekClient.chat(messages, tools, null);
                Map<String, Object> assistantMessage = extractAssistantMessage(resp);
                if (assistantMessage == null) break;
                messages.add(assistantMessage);

                List<Map<String, Object>> toolCalls = deepSeekClient.extractToolCalls(resp);
                if (toolCalls.isEmpty()) {
                    answer = deepSeekClient.extractContent(resp);
                    break;
                }

                for (Map<String, Object> toolCall : toolCalls) {
                    Map<String, Object> function = (Map<String, Object>) toolCall.get("function");
                    String toolName = function == null ? "" : String.valueOf(function.get("name"));
                    String arguments = function == null ? "{}" : String.valueOf(function.get("arguments"));
                    String callId = String.valueOf(toolCall.get("id"));

                    long start = System.currentTimeMillis();
                    Map<String, Object> output;
                    String status = "SUCCESS";
                    try {
                        output = functionToolRegistry.execute(toolName, arguments);
                    } catch (Exception e) {
                        output = new HashMap<>();
                        output.put("error", e.getMessage());
                        status = "ERROR";
                    }
                    long cost = System.currentTimeMillis() - start;

                    AgentStep agentStep = new AgentStep();
                    agentStep.setRunId(run.getId());
                    agentStep.setSeq(step);
                    agentStep.setToolName(toolName);
                    agentStep.setInputJson(arguments);
                    agentStep.setOutputJson(DeepSeekClient.toJson(output));
                    agentStep.setStatus(status);
                    agentStep.setCostMs(cost);
                    agentStepMapper.insert(agentStep);

                    FunctionCallLog log = new FunctionCallLog();
                    log.setRunId(run.getId());
                    log.setStepId(agentStep.getId());
                    log.setToolName(toolName);
                    log.setInputJson(arguments);
                    log.setOutputJson(DeepSeekClient.toJson(output));
                    log.setStatus(status);
                    log.setCostMs(cost);
                    functionCallLogMapper.insert(log);

                    if ("submitRecommendation".equals(toolName)) {
                        saveRecommendation(run, userId, guideTaskId, arguments);
                    }

                    Map<String, Object> toolMsg = new HashMap<>();
                    toolMsg.put("role", "tool");
                    toolMsg.put("tool_call_id", callId);
                    toolMsg.put("content", DeepSeekClient.toJson(output));
                    messages.add(toolMsg);
                }
            }

            run.setStatus("FINISHED");
            run.setAnswer(answer == null || answer.isBlank() ? "已完成导购分析，请查看推荐结果。" : answer);
            run.setFinishedAt(LocalDateTime.now());
            agentRunMapper.update(run);
            run.setSteps(agentStepMapper.findByRunId(run.getId()));

            if (guideTaskId != null) {
                GuideTask task = guideTaskMapper.findById(guideTaskId);
                if (task != null) {
                    task.setStatus("FINISHED");
                    guideTaskMapper.update(task);
                }
            }
            return run;
        } catch (Exception e) {
            run.setStatus("ERROR");
            run.setAnswer("Agent 执行异常：" + e.getMessage());
            run.setFinishedAt(LocalDateTime.now());
            agentRunMapper.update(run);
            if (guideTaskId != null) {
                GuideTask task = guideTaskMapper.findById(guideTaskId);
                if (task != null) {
                    task.setStatus("ERROR");
                    guideTaskMapper.update(task);
                }
            }
            throw e;
        }
    }

    private void saveRecommendation(AgentRun run, Long userId, Long guideTaskId, String arguments) {
        try {
            JSONObject args = JSONUtil.parseObj(arguments);
            cn.hutool.json.JSONArray productIdArray = args.getJSONArray("productIds");
            List<Long> productIds = productIdArray == null ? List.of() : productIdArray.toList(Long.class);
            String reason = args.getStr("reason", "");
            if (productIds == null) return;
            for (Long productId : productIds) {
                Product product = productMapper.findById(productId);
                if (product == null) continue;
                RecommendResult result = new RecommendResult();
                result.setGuideTaskId(guideTaskId);
                result.setRunId(run.getId());
                result.setUserId(userId);
                result.setProductId(productId);
                result.setReason(reason);
                result.setPriceSnapshot(product.getPrice());
                result.setStockSnapshot(product.getStock());
                result.setDiscountSnapshot(product.getOriginalPrice() == null ? "" : "原价" + product.getOriginalPrice());
                recommendResultMapper.insert(result);
            }
        } catch (Exception ignored) {
            // 推荐落库失败不影响主流程
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractAssistantMessage(Map<String, Object> resp) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.get("choices");
            if (choices == null || choices.isEmpty()) return null;
            return (Map<String, Object>) choices.get(0).get("message");
        } catch (Exception e) {
            return null;
        }
    }
}
