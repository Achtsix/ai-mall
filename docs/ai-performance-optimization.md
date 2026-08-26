# AI 性能优化配置指南

## P1-8: AI 导购响应时间优化

### 问题描述
当前 AI 导购平均响应时间约 60 秒，接近超时限制，用户体验较差。

### 优化策略

#### 1. 减少 Agent 最大步数
```yaml
aimall:
  agent:
    max-steps: 6  # 从 8 降到 6，减少冗余步骤
```

**影响**: 降低 25% 的最大执行时间，但仍足够完成大多数任务。

#### 2. 优化 RAG 检索数量
```yaml
aimall:
  agent:
    top-k: 3  # 从 5 降到 3，减少检索和 Token 数量
```

**影响**: 减少 40% 的上下文 Token，加快推理速度。

#### 3. 减少 max_tokens
```yaml
aimall:
  ai:
    openai:
      max-tokens: 2048  # 从 4096 降到 2048
```

**影响**: 减少输出 Token，加快响应速度。推荐理由通常不需要超过 2048 tokens。

#### 4. 降低 temperature
```yaml
aimall:
  ai:
    openai:
      temperature: 0.1  # 从 0.3 降到 0.1
```

**影响**: 更确定性的输出，减少模型"思考"时间。

#### 5. 使用更快的模型（可选）
```yaml
aimall:
  ai:
    openai:
      model: deepseek-chat  # 或其他更快的模型
```

**DeepSeek Chat** 通常比 GPT-4 快 2-3 倍。

#### 6. 启用并行工具调用（需要代码修改）
当前工具调用是串行的。如果模型支持并行工具调用，可以显著减少响应时间。

### 预期改进

| 优化项 | 时间节省 | 累计改进 |
|--------|---------|---------|
| max-steps: 6 | -15s | 45s |
| top-k: 3 | -8s | 37s |
| max-tokens: 2048 | -5s | 32s |
| temperature: 0.1 | -2s | 30s |
| 更快模型 | -10s | 20s |

**目标**: 从平均 60 秒降到 20-30 秒。

---

## P1-11: Agent 事实错误率优化

### 问题描述
Agent 在 2.33% 的推荐中存在价格/库存快照不实时的问题。

### 已修复
✅ **P1-1**: 推荐验证已在代码中添加实时状态、库存、价格检查（AgentEngine.java）

### 额外优化策略

#### 1. 增加工具调用时的实时验证提示
在 Prompt 模板中添加：
```
重要：每次推荐商品前，必须：
1. 调用 getProductDetail 获取最新价格和库存
2. 确认 stock > 0
3. 确认商品状态为上架
4. 在推荐理由中明确说明当前价格和库存状态
```

#### 2. 添加快照一致性检查日志
代码中已添加（P1-1 修复）：
```java
if (product.getStatus() != 1) {
    log.warn("推荐的商品已下架");
}
if (product.getStock() <= 0) {
    log.warn("推荐的商品无库存");
}
```

#### 3. 定期审计推荐质量
添加定时任务，每日检查推荐快照与当前数据的一致性：
```java
@Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点
public void auditRecommendations() {
    // 检查过去24小时的推荐
    List<RecommendResult> results = recommendResultMapper.findRecent(24);
    for (RecommendResult result : results) {
        Product current = productMapper.findById(result.getProductId());
        if (current == null || !result.getPriceSnapshot().equals(current.getPrice())) {
            log.warn("快照不一致: resultId={}, snapshot={}, current={}",
                result.getId(), result.getPriceSnapshot(), current.getPrice());
        }
    }
}
```

#### 4. 使用乐观锁防止并发问题
在 Product 表添加 version 字段：
```sql
ALTER TABLE product ADD COLUMN version INT DEFAULT 0;
```

更新时检查版本：
```java
UPDATE product 
SET price = ?, stock = ?, version = version + 1
WHERE id = ? AND version = ?
```

### 预期改进

| 优化项 | 错误率改进 | 累计改进 |
|--------|-----------|---------|
| 实时验证（已完成） | 2.33% → 1.0% | 57% |
| Prompt 优化 | 1.0% → 0.5% | 79% |
| 定期审计 | 监控和告警 | - |
| 乐观锁 | 0.5% → 0.2% | 91% |

**目标**: 从 2.33% 降到 0.2% 以下。

---

## 推荐的生产配置

```yaml
aimall:
  ai:
    openai:
      base-url: https://api.deepseek.com
      api-key: ${DEEPSEEK_API_KEY}
      model: deepseek-chat
      temperature: 0.1
      max-tokens: 2048
    embedding:
      base-url: https://api.deepseek.com
      api-key: ${DEEPSEEK_API_KEY}
      model: text-embedding-v1
  agent:
    max-steps: 6
    top-k: 3
    requests-per-minute: 20
  rag:
    vector-weight: 0.78
    lexical-weight: 0.22
    rerank-bonus: 0.08
```

---

## 监控指标

### 响应时间监控
```java
@Aspect
@Component
public class AgentPerformanceAspect {
    @Around("execution(* com.aimall.ai.AgentEngine.run(..))")
    public Object measurePerformance(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return pjp.proceed();
        } finally {
            long duration = System.currentTimeMillis() - start;
            if (duration > 30000) {
                log.warn("Agent 执行超过 30 秒: {}ms", duration);
            }
        }
    }
}
```

### 事实错误率监控
定期检查：
- 推荐时的快照 vs 当前数据
- 用户反馈的价格/库存不符
- Agent 工具调用日志

---

## 实施计划

### 第1天：配置优化（无需代码修改）
- 更新 application.yml
- 重启服务
- 监控响应时间和错误率

### 第2-3天：Prompt 优化
- 更新系统提示词
- 添加事实验证强调
- A/B 测试效果

### 第4-5天：监控和审计
- 添加性能监控切面
- 添加推荐质量审计任务
- 设置告警阈值

---

**最后更新**: 2026-08-26  
**状态**: P1-8 和 P1-11 优化指南，建议优先实施配置优化
