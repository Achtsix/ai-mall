package com.aimall.controller;

import cn.hutool.json.JSONUtil;
import com.aimall.ai.AiChatService;
import com.aimall.ai.AgentEngine;
import com.aimall.ai.RagService;
import com.aimall.ai.ToolExecutors;
import com.aimall.ai.AiRateLimiter;
import com.aimall.common.BusinessException;
import com.aimall.common.Result;
import com.aimall.common.UserContext;
import com.aimall.entity.AgentRun;
import com.aimall.entity.GuideTask;
import com.aimall.entity.RecommendResult;
import com.aimall.mapper.AgentRunMapper;
import com.aimall.mapper.AgentStepMapper;
import com.aimall.mapper.GuideTaskMapper;
import com.aimall.mapper.RecommendResultMapper;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiChatService aiChatService;
    private final AgentEngine agentEngine;
    private final RagService ragService;
    private final ToolExecutors toolExecutors;
    private final AgentRunMapper agentRunMapper;
    private final AgentStepMapper agentStepMapper;
    private final GuideTaskMapper guideTaskMapper;
    private final RecommendResultMapper recommendResultMapper;
    private final AiRateLimiter aiRateLimiter;

    public AiController(AiChatService aiChatService,
                        AgentEngine agentEngine,
                        RagService ragService,
                        ToolExecutors toolExecutors,
                        AgentRunMapper agentRunMapper,
                        AgentStepMapper agentStepMapper,
                        GuideTaskMapper guideTaskMapper,
                        RecommendResultMapper recommendResultMapper,
                        AiRateLimiter aiRateLimiter) {
        this.aiChatService = aiChatService;
        this.agentEngine = agentEngine;
        this.ragService = ragService;
        this.toolExecutors = toolExecutors;
        this.agentRunMapper = agentRunMapper;
        this.agentStepMapper = agentStepMapper;
        this.guideTaskMapper = guideTaskMapper;
        this.recommendResultMapper = recommendResultMapper;
        this.aiRateLimiter = aiRateLimiter;
    }

    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(@RequestBody Map<String, Object> req) {
        aiRateLimiter.check(UserContext.getUserId());
        String question = req.get("question") == null ? "" : String.valueOf(req.get("question"));
        String questionType = req.get("questionType") == null ? "PRODUCT_QA" : String.valueOf(req.get("questionType"));
        Long productId = req.get("productId") == null ? null : Long.valueOf(req.get("productId").toString());
        Long guideTaskId = req.get("guideTaskId") == null ? null : Long.valueOf(req.get("guideTaskId").toString());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> history = req.get("history") instanceof List<?> list
                ? (List<Map<String, Object>>) (List<?>) list : List.of();
        return Result.ok(aiChatService.chat(question, questionType, productId, guideTaskId, history));
    }

    @PostMapping("/guide")
    public Result<Map<String, Object>> guide(@RequestBody Map<String, Object> req) {
        Long userId = UserContext.getUserId();
        aiRateLimiter.check(userId);
        GuideTask task = new GuideTask();
        task.setUserId(userId);
        task.setQuestion(req.get("question") == null ? "" : String.valueOf(req.get("question")));
        task.setStatus("PROCESSING");
        guideTaskMapper.insert(task);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> history = req.get("history") instanceof List<?> list
                ? (List<Map<String, Object>>) (List<?>) list : List.of();
        AgentRun run = agentEngine.run(userId, task.getQuestion(), task.getId(), history);
        Map<String, Object> result = new HashMap<>();
        result.put("taskId", task.getId());
        result.put("runId", run.getId());
        result.put("answer", run.getAnswer());
        result.put("recommendations", recommendResultMapper.findByTaskId(task.getId()));
        return Result.ok(result);
    }

    @GetMapping("/guide/history")
    public Result<List<GuideTask>> guideHistory() {
        return Result.ok(guideTaskMapper.findByUserId(UserContext.getUserId()));
    }

    @GetMapping("/guide/{taskId}/recommendations")
    public Result<List<RecommendResult>> recommendations(@PathVariable Long taskId) {
        GuideTask task = guideTaskMapper.findById(taskId);
        if (task == null || !UserContext.getUserId().equals(task.getUserId())) {
            throw new BusinessException(404, "导购任务不存在");
        }
        return Result.ok(recommendResultMapper.findByTaskId(taskId));
    }

    @GetMapping("/agent/runs")
    public Result<List<AgentRun>> agentRuns() {
        List<AgentRun> runs = agentRunMapper.findByUserId(UserContext.getUserId());
        runs.forEach(r -> r.setSteps(agentStepMapper.findByRunId(r.getId())));
        return Result.ok(runs);
    }

    @GetMapping("/agent/runs/{id}")
    public Result<AgentRun> agentRunDetail(@PathVariable Long id) {
        AgentRun run = agentRunMapper.findById(id);
        if (run == null || !UserContext.getUserId().equals(run.getUserId())) {
            throw new BusinessException(404, "Agent 运行记录不存在");
        }
        run.setSteps(agentStepMapper.findByRunId(id));
        return Result.ok(run);
    }

    @PostMapping("/knowledge/reindex")
    public Result<Void> reindexKnowledge() {
        ragService.reindexAll();
        return Result.ok();
    }

    @GetMapping("/config")
    public Result<Map<String, Object>> aiConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("model", agentEngine.getActiveModel());
        config.put("provider", "OPENAI_COMPATIBLE");
        config.put("configured", agentEngine.isConfigured());
        return Result.ok(config);
    }

    // 工具中心对外调试接口，也供 Agent 内部调用
    @PostMapping("/tools/searchProducts")
    public Result<Map<String, Object>> searchProducts(@RequestBody String body) {
        return Result.ok(toolExecutors.searchProducts(body));
    }

    @GetMapping("/tools/getProductDetail")
    public Result<Map<String, Object>> getProductDetail(@RequestParam Long productId) {
        return Result.ok(toolExecutors.getProductDetail("{\"productId\":" + productId + "}"));
    }

    @GetMapping("/tools/getUserProfile")
    public Result<Map<String, Object>> getUserProfile(@RequestParam Long userId) {
        return Result.ok(toolExecutors.getUserProfile("{\"userId\":" + userId + "}"));
    }

    @PostMapping("/tools/getSimilarProducts")
    public Result<Map<String, Object>> getSimilarProducts(@RequestBody String body) {
        return Result.ok(toolExecutors.getSimilarProducts(body));
    }

    @GetMapping("/tools/getOrderStatus")
    public Result<Map<String, Object>> getOrderStatus(@RequestParam String orderNo) {
        return Result.ok(toolExecutors.getOrderStatus("{\"orderNo\":\"" + orderNo + "\"}"));
    }

    @PostMapping("/tools/getAfterSaleRule")
    public Result<Map<String, Object>> getAfterSaleRule(@RequestBody String body) {
        return Result.ok(toolExecutors.getAfterSaleRule(body));
    }

    @PostMapping("/tools/submitRecommendation")
    public Result<Map<String, Object>> submitRecommendation(@RequestBody String body) {
        return Result.ok(toolExecutors.submitRecommendation(body));
    }
}
