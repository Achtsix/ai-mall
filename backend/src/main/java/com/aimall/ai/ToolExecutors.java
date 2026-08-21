package com.aimall.ai;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aimall.entity.AfterSaleRule;
import com.aimall.entity.Order;
import com.aimall.entity.Product;
import com.aimall.mapper.AfterSaleRuleMapper;
import com.aimall.mapper.OrderMapper;
import com.aimall.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ToolExecutors {

    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final AfterSaleRuleMapper afterSaleRuleMapper;

    public ToolExecutors(ProductMapper productMapper, OrderMapper orderMapper, AfterSaleRuleMapper afterSaleRuleMapper) {
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
        this.afterSaleRuleMapper = afterSaleRuleMapper;
    }

    public Map<String, Object> searchProducts(String argsJson) {
        JSONObject args = JSONUtil.parseObj(argsJson);
        Long categoryId = args.getLong("categoryId");
        Long brandId = args.getLong("brandId");
        String keyword = args.getStr("keywords");
        BigDecimal minPrice = args.getBigDecimal("minPrice");
        BigDecimal maxPrice = args.getBigDecimal("maxPrice");
        List<Product> products = productMapper.search(categoryId, brandId, keyword, minPrice, maxPrice);
        Map<String, Object> result = new HashMap<>();
        result.put("products", products.stream().limit(20).map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("price", p.getPrice());
            m.put("stock", p.getStock());
            m.put("sales", p.getSales());
            m.put("mainImage", p.getMainImage());
            return m;
        }).toList());
        return result;
    }

    public Map<String, Object> getProductDetail(String argsJson) {
        JSONObject args = JSONUtil.parseObj(argsJson);
        Long productId = args.getLong("productId");
        Product p = productMapper.findById(productId);
        Map<String, Object> result = new HashMap<>();
        if (p == null) {
            result.put("error", "商品不存在");
            return result;
        }
        result.put("id", p.getId());
        result.put("name", p.getName());
        result.put("subtitle", p.getSubtitle());
        result.put("price", p.getPrice());
        result.put("originalPrice", p.getOriginalPrice());
        result.put("stock", p.getStock());
        result.put("sales", p.getSales());
        result.put("params", p.getParamsJson());
        result.put("detailHtml", p.getDetailHtml());
        return result;
    }

    public Map<String, Object> getUserProfile(String argsJson) {
        JSONObject args = JSONUtil.parseObj(argsJson);
        Long userId = args.getLong("userId");
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("preferences", List.of("数码产品", "高性价比"));
        result.put("orderCount", 0);
        return result;
    }

    public Map<String, Object> getSimilarProducts(String argsJson) {
        JSONObject args = JSONUtil.parseObj(argsJson);
        Long productId = args.getLong("productId");
        Integer limit = args.getInt("limit", 5);
        Product base = productMapper.findById(productId);
        Map<String, Object> result = new HashMap<>();
        if (base == null) {
            result.put("products", List.of());
            return result;
        }
        List<Product> products = productMapper.search(base.getCategoryId(), null, null, null, null);
        result.put("products", products.stream()
                .filter(p -> !p.getId().equals(productId))
                .limit(limit == null ? 5 : limit)
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", p.getId());
                    m.put("name", p.getName());
                    m.put("price", p.getPrice());
                    m.put("stock", p.getStock());
                    return m;
                }).toList());
        return result;
    }

    public Map<String, Object> getOrderStatus(String argsJson) {
        JSONObject args = JSONUtil.parseObj(argsJson);
        String orderNo = args.getStr("orderNo");
        Order order = orderMapper.findByOrderNo(orderNo);
        Map<String, Object> result = new HashMap<>();
        if (order == null) {
            result.put("error", "订单不存在");
            return result;
        }
        result.put("orderNo", order.getOrderNo());
        result.put("status", order.getStatus());
        result.put("payAmount", order.getPayAmount());
        result.put("createTime", order.getCreateTime());
        return result;
    }

    public Map<String, Object> getAfterSaleRule(String argsJson) {
        JSONObject args = JSONUtil.parseObj(argsJson);
        String question = args.getStr("question", "");
        List<AfterSaleRule> rules = afterSaleRuleMapper.search(question);
        Map<String, Object> result = new HashMap<>();
        result.put("rules", rules);
        return result;
    }

    public Map<String, Object> submitRecommendation(String argsJson) {
        // 实际落库由 AgentEngine 在拿到结果后二次校验并写入 recommend_result
        JSONObject args = JSONUtil.parseObj(argsJson);
        cn.hutool.json.JSONArray productIdArray = args.getJSONArray("productIds");
        List<Long> productIds = productIdArray == null ? List.of() : productIdArray.toList(Long.class);
        String reason = args.getStr("reason");
        Map<String, Object> result = new HashMap<>();
        result.put("ok", true);
        result.put("productIds", productIds == null ? List.of() : productIds);
        result.put("reason", reason);
        return result;
    }
}
