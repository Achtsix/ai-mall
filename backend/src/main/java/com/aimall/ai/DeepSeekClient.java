package com.aimall.ai;

import cn.hutool.json.JSONUtil;
import com.aimall.entity.ModelConfig;
import com.aimall.mapper.ModelConfigMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.aimall.common.BusinessException;

import java.util.List;
import java.util.Map;

/**
 * DeepSeek OpenAI 兼容客户端。
 * Model metadata may come from model_config, but credentials are environment-only.
 */
@Service
public class DeepSeekClient {

    private static final Logger log = LoggerFactory.getLogger(DeepSeekClient.class);

    private final ModelConfigMapper modelConfigMapper;
    private final RestClient restClient;

    @Value("${spring.ai.openai.base-url:https://api.deepseek.com}")
    private String defaultBaseUrl;

    @Value("${spring.ai.openai.api-key:}")
    private String defaultApiKey;

    @Value("${aimall.ai.openai.base-url:https://api.openai.com/v1}")
    private String openAiBaseUrl;

    @Value("${aimall.ai.openai.api-key:}")
    private String openAiApiKey;

    @Value("${aimall.ai.openai.model:gpt-5.6}")
    private String openAiModel;

    @Value("${aimall.ai.openai.temperature:0.3}")
    private double openAiTemperature;

    @Value("${aimall.ai.openai.max-tokens:4096}")
    private int openAiMaxTokens;

    @Value("${aimall.ai.embedding.base-url:${aimall.ai.openai.base-url:https://api.openai.com/v1}}")
    private String embeddingBaseUrl;

    @Value("${aimall.ai.embedding.api-key:${aimall.ai.openai.api-key:}}")
    private String embeddingApiKey;

    @Value("${aimall.ai.embedding.model:text-embedding-3-small}")
    private String embeddingModel;

    public DeepSeekClient(ModelConfigMapper modelConfigMapper) {
        this.modelConfigMapper = modelConfigMapper;
        this.restClient = RestClient.create();
    }

    public Map<String, Object> chat(List<Map<String, Object>> messages,
                                    List<Map<String, Object>> tools,
                                    Double temperature) {
        ModelConfig config = modelConfigMapper.findEnabled();
        boolean openAiEnvironmentConfigured = isConfigured(openAiApiKey);
        String baseUrl;
        String apiKey;
        String model;
        double temp;
        int maxTokens;
        if (openAiEnvironmentConfigured) {
            // Explicit environment configuration wins over the database demo row.
            baseUrl = openAiBaseUrl;
            apiKey = openAiApiKey;
            model = openAiModel;
            temp = temperature != null ? temperature : openAiTemperature;
            maxTokens = openAiMaxTokens;
        } else if (isConfigured(defaultApiKey)) {
            baseUrl = config == null ? defaultBaseUrl : firstNonBlank(config.getBaseUrl(), defaultBaseUrl);
            apiKey = defaultApiKey;
            model = config == null ? "deepseek-chat" : firstNonBlank(config.getModel(), "deepseek-chat");
            temp = temperature != null ? temperature : (config == null || config.getTemperature() == null ? 0.7 : config.getTemperature().doubleValue());
            maxTokens = config == null || config.getMaxTokens() == null ? 4096 : config.getMaxTokens();
        } else {
            throw new BusinessException(503, "未配置 AI API Key，请设置 OPENAI_API_KEY 或 DEEPSEEK_API_KEY");
        }

        Map<String, Object> body = new java.util.HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        if (model.toLowerCase().startsWith("gpt-5")) {
            // Newer GPT-5-compatible endpoints use the completion-token name and may reject temperature.
            body.put("max_completion_tokens", maxTokens);
        } else {
            body.put("temperature", temp);
            body.put("max_tokens", maxTokens);
        }
        if (tools != null && !tools.isEmpty()) {
            body.put("tools", tools);
        }

        String url = baseUrl.endsWith("/") ? baseUrl + "chat/completions" : baseUrl + "/chat/completions";
        Map<String, Object> resp;
        try {
            resp = restClient.post()
                    .uri(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientResponseException e) {
            // P0 安全修复：脱敏响应体，避免 API 密钥泄露到日志
            String safeResponseBody = sanitizeLogContent(e.getResponseBodyAsString());
            log.error("AI upstream request failed: status={}, url={}, body={}",
                    e.getStatusCode().value(), url, safeResponseBody);
            int status = e.getStatusCode().value();
            String message = status == 401 || status == 403
                    ? "AI API Key 无效或无权限，请重新配置"
                    : status == 404
                    ? "AI API 地址或模型不存在，请检查 Base URL 和模型名"
                    : "AI 请求参数被上游拒绝，请检查模型和工具配置（HTTP " + status + "）";
            throw new BusinessException(502, message);
        } catch (Exception e) {
            throw new BusinessException(502, "AI 服务调用失败，请检查 API 地址、密钥和模型配置");
        }
        if (resp == null) {
            throw new RuntimeException("DeepSeek API 返回为空");
        }
        return resp;
    }

    public double[] embed(String text) {
        double[] remote = remoteEmbed(text);
        if (remote != null) return remote;
        return localEmbed(text);
    }

    public String getEmbeddingMode() {
        return isConfigured(embeddingApiKey) ? "REMOTE:" + embeddingModel : "LOCAL_FALLBACK:256";
    }

    /** Uses an OpenAI-compatible embeddings endpoint when configured. */
    @SuppressWarnings("unchecked")
    private double[] remoteEmbed(String text) {
        if (!isConfigured(embeddingApiKey) || text == null || text.isBlank()) return null;
        try {
            Map<String, Object> body = new java.util.HashMap<>();
            body.put("model", embeddingModel);
            body.put("input", text);
            String url = embeddingBaseUrl.endsWith("/") ? embeddingBaseUrl + "embeddings" : embeddingBaseUrl + "/embeddings";
            Map<String, Object> response = restClient.post().uri(url)
                    .header("Authorization", "Bearer " + embeddingApiKey)
                    .contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(Map.class);
            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
            if (data == null || data.isEmpty()) return null;
            List<Number> values = (List<Number>) data.get(0).get("embedding");
            if (values == null || values.isEmpty()) return null;
            double[] vector = new double[values.size()];
            for (int i = 0; i < values.size(); i++) vector[i] = values.get(i).doubleValue();
            return vector;
        } catch (Exception e) {
            log.warn("Embedding API unavailable, using local fallback: {}", e.getMessage());
            return null;
        }
    }

    /** Deterministic fallback for offline development and providers without embeddings. */
    private double[] localEmbed(String text) {
        double[] vector = new double[256];
        if (text == null || text.isBlank()) return vector;
        String normalized = text.toLowerCase().replaceAll("[^a-z0-9\\u4e00-\\u9fa5]", " ");
        java.util.List<String> tokens = new java.util.ArrayList<>(java.util.Arrays.asList(normalized.split("\\s+")));
        for (int i = 0; i + 1 < normalized.length(); i++) {
            char a = normalized.charAt(i), b = normalized.charAt(i + 1);
            if (a >= '\u4e00' && a <= '\u9fa5' && b >= '\u4e00' && b <= '\u9fa5') tokens.add("" + a + b);
        }
        for (String token : tokens) {
            if (token.isBlank()) continue;
            int hash = token.hashCode();
            int idx = Math.floorMod(hash, vector.length);
            vector[idx] += 1.0;
        }
        // 归一化
        double norm = 0;
        for (double v : vector) norm += v * v;
        norm = Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) vector[i] /= norm;
        }
        return vector;
    }

    public String extractContent(Map<String, Object> resp) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.get("choices");
            if (choices == null || choices.isEmpty()) return "";
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            if (message == null) return "";
            Object content = message.get("content");
            return content == null ? "" : content.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public String getActiveModel() {
        if (isConfigured(openAiApiKey)) return openAiModel;
        ModelConfig config = modelConfigMapper.findEnabled();
        if (isConfigured(defaultApiKey)) return config == null ? "deepseek-chat" : firstNonBlank(config.getModel(), "deepseek-chat");
        return "deepseek-chat";
    }

    public boolean isConfigured() {
        if (isConfigured(openAiApiKey)) return true;
        return isConfigured(defaultApiKey);
    }

    private static boolean isConfigured(String value) {
        return value != null && !value.isBlank() && !value.contains("${") && !value.startsWith("sk-your-");
    }

    private static String firstNonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    /**
     * P0 安全修复：脱敏日志内容，移除敏感信息
     * 移除可能的 API Key、Token 等敏感字段
     */
    private String sanitizeLogContent(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        // 脱敏常见的敏感字段
        String sanitized = content;

        // 脱敏 Bearer Token
        sanitized = sanitized.replaceAll("Bearer\\s+[A-Za-z0-9_\\-\\.]+", "Bearer [REDACTED]");

        // 脱敏 API Key 格式（sk-xxx, key-xxx 等）
        sanitized = sanitized.replaceAll("(\"api[_-]?key\"\\s*:\\s*\")([^\"]+)(\")", "$1[REDACTED]$3");
        sanitized = sanitized.replaceAll("(\"token\"\\s*:\\s*\")([^\"]+)(\")", "$1[REDACTED]$3");
        sanitized = sanitized.replaceAll("(\"authorization\"\\s*:\\s*\")([^\"]+)(\")", "$1[REDACTED]$3");

        // 脱敏 sk- 开头的密钥
        sanitized = sanitized.replaceAll("sk-[A-Za-z0-9]{20,}", "sk-[REDACTED]");

        // 限制日志长度，避免超大响应体
        if (sanitized.length() > 1000) {
            sanitized = sanitized.substring(0, 1000) + "... [truncated]";
        }

        return sanitized;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> extractToolCalls(Map<String, Object> resp) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.get("choices");
            if (choices == null || choices.isEmpty()) return List.of();
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            if (message == null) return List.of();
            Object toolCalls = message.get("tool_calls");
            if (toolCalls == null) return List.of();
            return (List<Map<String, Object>>) toolCalls;
        } catch (Exception e) {
            return List.of();
        }
    }

    public static String toJson(Object obj) {
        return JSONUtil.toJsonStr(obj);
    }
}
