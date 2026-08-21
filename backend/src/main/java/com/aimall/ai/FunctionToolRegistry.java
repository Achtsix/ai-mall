package com.aimall.ai;

import cn.hutool.json.JSONUtil;
import com.aimall.entity.FunctionTool;
import com.aimall.mapper.FunctionToolMapper;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class FunctionToolRegistry {

    private final FunctionToolMapper functionToolMapper;
    private final ToolExecutors toolExecutors;
    private final Map<String, Function<String, Map<String, Object>>> executors = new HashMap<>();

    public FunctionToolRegistry(FunctionToolMapper functionToolMapper, ToolExecutors toolExecutors) {
        this.functionToolMapper = functionToolMapper;
        this.toolExecutors = toolExecutors;
        executors.put("searchProducts", toolExecutors::searchProducts);
        executors.put("getProductDetail", toolExecutors::getProductDetail);
        executors.put("getUserProfile", toolExecutors::getUserProfile);
        executors.put("getSimilarProducts", toolExecutors::getSimilarProducts);
        executors.put("getOrderStatus", toolExecutors::getOrderStatus);
        executors.put("getAfterSaleRule", toolExecutors::getAfterSaleRule);
        executors.put("submitRecommendation", toolExecutors::submitRecommendation);
    }

    public List<Map<String, Object>> buildToolDefinitions() {
        return functionToolMapper.findEnabled().stream().map(this::toDefinition).toList();
    }

    public Map<String, Object> execute(String name, String arguments) {
        Function<String, Map<String, Object>> fn = executors.get(name);
        if (fn == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "未知工具: " + name);
            return err;
        }
        return fn.apply(arguments);
    }

    private Map<String, Object> toDefinition(FunctionTool tool) {
        Map<String, Object> function = new HashMap<>();
        function.put("name", tool.getName());
        function.put("description", tool.getDescription());
        Object parameters;
        try {
            parameters = normalizeParameters(JSONUtil.parse(tool.getRequestSchema()));
        } catch (Exception e) {
            parameters = Map.of("type", "object", "properties", Map.of());
        }
        function.put("parameters", parameters);
        Map<String, Object> definition = new HashMap<>();
        definition.put("type", "function");
        definition.put("function", function);
        return definition;
    }

    /** Convert legacy example-value request schemas into JSON Schema. */
    private Object normalizeParameters(Object parsed) {
        if (!(parsed instanceof Map<?, ?> raw)) {
            return Map.of("type", "object", "properties", Map.of());
        }
        if (raw.containsKey("type") && raw.containsKey("properties")) return parsed;
        Map<String, Object> properties = new LinkedHashMap<>();
        raw.forEach((key, value) -> properties.put(String.valueOf(key), schemaFor(value)));
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("properties", properties);
        return schema;
    }

    private Map<String, Object> schemaFor(Object value) {
        Map<String, Object> schema = new LinkedHashMap<>();
        if (value instanceof Boolean) schema.put("type", "boolean");
        else if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long) schema.put("type", "integer");
        else if (value instanceof Number) schema.put("type", "number");
        else if (value instanceof Collection<?> collection) {
            schema.put("type", "array");
            Object first = collection.stream().findFirst().orElse(null);
            schema.put("items", first == null ? Map.of("type", "string") : schemaFor(first));
        } else if (value instanceof Map<?, ?>) {
            schema.put("type", "object");
            schema.put("properties", normalizeParameters(value));
        } else schema.put("type", "string");
        return schema;
    }
}
