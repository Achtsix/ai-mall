package com.aimall.ai;

import com.aimall.common.UserContext;
import com.aimall.entity.AfterSaleRule;
import com.aimall.entity.HighFreqQuestion;
import com.aimall.entity.Product;
import com.aimall.entity.PromptTemplate;
import com.aimall.mapper.AfterSaleRuleMapper;
import com.aimall.mapper.HighFreqQuestionMapper;
import com.aimall.mapper.ProductMapper;
import com.aimall.mapper.PromptTemplateMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiChatService {
    private final AgentEngine agentEngine;
    private final RagService ragService;
    private final DeepSeekClient deepSeekClient;
    private final AfterSaleRuleMapper afterSaleRuleMapper;
    private final HighFreqQuestionMapper highFreqQuestionMapper;
    private final PromptTemplateMapper promptTemplateMapper;
    private final ToolExecutors toolExecutors;
    private final ProductMapper productMapper;

    public AiChatService(AgentEngine agentEngine, RagService ragService, DeepSeekClient deepSeekClient,
                         AfterSaleRuleMapper afterSaleRuleMapper, HighFreqQuestionMapper highFreqQuestionMapper,
                         PromptTemplateMapper promptTemplateMapper, ToolExecutors toolExecutors,
                         ProductMapper productMapper) {
        this.agentEngine = agentEngine;
        this.ragService = ragService;
        this.deepSeekClient = deepSeekClient;
        this.afterSaleRuleMapper = afterSaleRuleMapper;
        this.highFreqQuestionMapper = highFreqQuestionMapper;
        this.promptTemplateMapper = promptTemplateMapper;
        this.toolExecutors = toolExecutors;
        this.productMapper = productMapper;
    }

    public Map<String, Object> chat(String question, String questionType, Long productId, Long guideTaskId) {
        return chat(question, questionType, productId, guideTaskId, List.of());
    }

    public Map<String, Object> chat(String question, String questionType, Long productId, Long guideTaskId,
                                    List<Map<String, Object>> history) {
        recordQuestion(question);
        Long userId = UserContext.getUserId();
        if ("GUIDE".equalsIgnoreCase(questionType)) {
            var run = agentEngine.run(userId, question, guideTaskId, history);
            Map<String, Object> result = new HashMap<>();
            result.put("runId", run.getId());
            result.put("answer", run.getAnswer());
            result.put("steps", run.getSteps());
            return result;
        }
        if ("PRICE_STOCK".equalsIgnoreCase(questionType) || "ORDER_STATUS".equalsIgnoreCase(questionType)) {
            Map<String, Object> result = new HashMap<>();
            if ("PRICE_STOCK".equalsIgnoreCase(questionType) && productId != null) {
                Map<String, Object> data = toolExecutors.getProductDetail("{\"productId\":" + productId + "}");
                result.put("data", data);
                result.put("answer", "实时价格：" + data.get("price") + " 元，库存：" + data.get("stock") + " 件。");
            } else if ("ORDER_STATUS".equalsIgnoreCase(questionType)) {
                result.put("answer", "请提供订单号以查询真实订单状态。");
            } else {
                result.put("answer", "请提供商品 ID 以查询实时价格和库存。");
            }
            return result;
        }
        if ("AFTER_SALE".equalsIgnoreCase(questionType)) {
            List<AfterSaleRule> rules = afterSaleRuleMapper.search(question);
            Map<String, Object> result = new HashMap<>();
            result.put("answer", rules.isEmpty() ? "暂无匹配的售后规则，请咨询人工客服。" : rules.get(0).getContent());
            result.put("rules", rules);
            return result;
        }
        return productKnowledgeChat(question, productId, history);
    }

    private Map<String, Object> productKnowledgeChat(String question, Long productId,
                                                       List<Map<String, Object>> history) {
        Product product = productId == null ? null : productMapper.findById(productId);
        String context = ragService.buildContext(question, productId, 8);
        PromptTemplate qa = promptTemplateMapper.findLatestByType("QA");
        PromptTemplate qaSystem = promptTemplateMapper.findLatestByType("QA_SYSTEM");
        String system = qaSystem != null ? qaSystem.getContent()
                : "你是商品详情页的智能导购助手。请基于提供的商品资料和实时数据自然地回答用户问题。";
        if (qa != null && qa.getContent() != null && !qa.getContent().isBlank()) {
            system += "\n\n回答风格要求：" + qa.getContent();
        }
        system += "\n你正在回答当前商品的问题，不得混入其他商品信息。"
                + "价格和库存只能使用实时商品数据；资料未覆盖的内容要明确说明，不得编造。"
                + "请像普通对话模型一样直接回答，不要要求用户重复提供商品名称。"
                + "如果实时商品信息或知识库已经包含答案，请直接给出具体结论和关键数值，不要先发欢迎语或反问。";

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", system));
        addRecentHistory(messages, history, 10);
        String productInfo = product == null ? "当前商品信息不可用。" : formatProduct(product);
        String knowledge = context.isBlank() ? "未检索到当前商品的知识库资料。" : context;
        String userPrompt = "当前商品实时信息：\n" + productInfo
                + "\n\n当前商品知识库资料：\n" + knowledge
                + "\n\n用户问题：\n" + question;
        messages.add(Map.of("role", "user", "content", userPrompt));
        Map<String, Object> resp = deepSeekClient.chat(messages, null, 0.3);
        Map<String, Object> result = new HashMap<>();
        result.put("answer", deepSeekClient.extractContent(resp));
        result.put("context", context);
        return result;
    }

    private void addRecentHistory(List<Map<String, Object>> messages, List<Map<String, Object>> history, int maxTurns) {
        if (history == null || history.isEmpty()) return;
        List<Map<String, Object>> valid = history.stream().filter(item -> {
            String role = String.valueOf(item.getOrDefault("role", ""));
            Object content = item.get("content");
            return ("user".equals(role) || "assistant".equals(role)) && content != null && !content.toString().isBlank();
        }).toList();
        int start = Math.max(0, valid.size() - maxTurns * 2);
        for (int i = start; i < valid.size(); i++) {
            Map<String, Object> item = valid.get(i);
            messages.add(Map.of("role", String.valueOf(item.get("role")), "content", cleanContent(String.valueOf(item.get("content")))));
        }
    }

    private String cleanContent(String content) {
        return content == null ? "" : content.replaceAll("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");
    }

    private String formatProduct(Product p) {
        String detail = p.getDetailHtml() == null ? "暂无" : p.getDetailHtml().replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
        return "商品名称：" + p.getName() + "\n商品ID：" + p.getId()
                + "\n当前价格：" + p.getPrice() + " 元\n当前库存：" + p.getStock() + " 件"
                + "\n商品卖点：" + (p.getSubtitle() == null ? "暂无" : p.getSubtitle())
                + "\n规格参数：" + (p.getParamsJson() == null ? "暂无" : p.getParamsJson())
                + "\n商品详情：" + detail;
    }

    private void recordQuestion(String question) {
        try {
            HighFreqQuestion exist = highFreqQuestionMapper.findByQuestion(question);
            if (exist != null) highFreqQuestionMapper.increaseCount(exist.getId());
            else {
                HighFreqQuestion q = new HighFreqQuestion();
                q.setQuestion(question);
                highFreqQuestionMapper.insert(q);
            }
        } catch (Exception ignored) {
        }
    }
}
