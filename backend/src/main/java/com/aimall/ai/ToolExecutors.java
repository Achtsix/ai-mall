package com.aimall.ai;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.aimall.common.UserContext;
import com.aimall.entity.AfterSaleRule;
import com.aimall.entity.Order;
import com.aimall.entity.OrderItem;
import com.aimall.entity.Product;
import com.aimall.mapper.AfterSaleRuleMapper;
import com.aimall.mapper.OrderMapper;
import com.aimall.mapper.OrderItemMapper;
import com.aimall.mapper.ProductFavoriteMapper;
import com.aimall.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ToolExecutors {

    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductFavoriteMapper productFavoriteMapper;
    private final AfterSaleRuleMapper afterSaleRuleMapper;

    public ToolExecutors(ProductMapper productMapper, OrderMapper orderMapper, OrderItemMapper orderItemMapper,
                         ProductFavoriteMapper productFavoriteMapper, AfterSaleRuleMapper afterSaleRuleMapper) {
        this.productMapper = productMapper;
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.productFavoriteMapper = productFavoriteMapper;
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

        // P0 安全修复：验证用户权限，只能查询自己的资料
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "未登录");
            return error;
        }
        if (!currentUserId.equals(userId)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "无权访问其他用户的资料");
            return error;
        }

        List<Order> orders = userId == null ? List.of() : orderMapper.findByUserId(userId);
        List<OrderItem> purchasedItems = userId == null ? List.of() : orderItemMapper.findByUserId(userId);
        List<com.aimall.entity.ProductFavorite> favorites = userId == null ? List.of() : productFavoriteMapper.findByUserId(userId);

        Map<String, Integer> categoryCounts = new HashMap<>();
        Map<String, Integer> brandCounts = new HashMap<>();
        Set<String> purchasedProducts = new LinkedHashSet<>();
        for (OrderItem item : purchasedItems) {
            Product product = productMapper.findById(item.getProductId());
            if (product == null) continue;
            purchasedProducts.add(product.getName());
            if (product.getCategoryName() != null) categoryCounts.merge(product.getCategoryName(), quantity(item), Integer::sum);
            if (product.getBrandName() != null) brandCounts.merge(product.getBrandName(), quantity(item), Integer::sum);
        }
        List<String> preferredCategories = topKeys(categoryCounts);
        List<String> preferredBrands = topKeys(brandCounts);
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("source", "DATABASE_DERIVED");
        result.put("preferences", preferredCategories);
        result.put("preferredBrands", preferredBrands);
        result.put("orderCount", orders.size());
        result.put("favoriteCount", favorites.size());
        result.put("purchasedProducts", new ArrayList<>(purchasedProducts).stream().limit(5).toList());
        return result;
    }

    private int quantity(OrderItem item) {
        return item.getQuantity() == null || item.getQuantity() < 1 ? 1 : item.getQuantity();
    }

    private List<String> topKeys(Map<String, Integer> counts) {
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()).thenComparing(Map.Entry::getKey))
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();
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

        // P0 安全修复：验证订单所有权，只能查询自己的订单
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            result.put("error", "未登录");
            return result;
        }
        if (!currentUserId.equals(order.getUserId())) {
            result.put("error", "无权访问此订单");
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
