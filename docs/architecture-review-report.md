# 架构审查报告

## 执行摘要

**整体架构质量**: 6.5/10

AI Mall 项目展现了清晰的 AI 层设计和标准的 Spring Boot 分层架构，但在分层违规、领域边界和可扩展性方面存在关键问题。

### 核心优势
- 清晰的 AI 层职责分离
- 灵活的多提供商 LLM 支持
- 强大的 Agent 执行可观测性
- 标准的 Spring Boot 分层

### 关键问题
- AI 层绕过服务层（违反分层）
- Controller 直接使用 Mapper（破坏封装）
- AI 领域过于宽泛（需要子域拆分）
- 工具注册硬编码（可扩展性问题）

---

## 1. AI 层设计

### 1.1 核心组件分析

#### AgentEngine.java - Agent 编排层
**职责**: 多步骤 ReAct 风格 Agent 模式与工具调用

**优势**:
- ✅ 实现了清晰的 Agent 循环（最多 8 步）
- ✅ 正确持久化 AgentRun 和 AgentStep 以实现可观测性
- ✅ 与 GuideTask 和 RecommendResult 集成良好
- ✅ 编排和执行之间清晰分离

**问题**:
- ❌ 未将用户上下文传递给工具执行器
- ❌ 错误处理使用 `ignored` 注释静默失败
- ❌ 推荐验证不完整（未检查价格/库存漂移）

**推荐**:
```java
// 将 userId 传递给工具
String result = toolExecutors.execute(
    toolName, 
    arguments, 
    userId  // 添加用户上下文
);
```

#### AiChatService.java - AI 功能的业务门面
**职责**: 路由不同问题类型（GUIDE、PRICE_STOCK、ORDER_STATUS、AFTER_SALE、PRODUCT_QA）

**优势**:
- ✅ 清晰的问题类型分类
- ✅ 为 GUIDE 模式集成 AgentEngine，为产品问答直接使用 RAG
- ✅ 结合实时产品数据与知识库上下文
- ✅ 记录高频问题用于分析

**架构**:
```
AiChatService
├── GUIDE → AgentEngine（工具调用）
├── PRICE_STOCK → 直接 DB 查询
├── PRODUCT_QA → RAG 检索
├── ORDER_STATUS → 直接 DB 查询
└── AFTER_SALE → RAG + 规则
```

#### DeepSeekClient.java - OpenAI 兼容 API 客户端
**职责**: 抽象多个 LLM 提供商

**优势**:
- ✅ 支持多个提供商（DeepSeek、OpenAI、自定义端点）
- ✅ 灵活的配置层次：环境变量覆盖数据库配置
- ✅ 实现远程和本地回退 embedding（256 维确定性哈希）
- ✅ 健壮的错误处理与业务友好的错误消息
- ✅ 模型特定参数适配（GPT-5 vs DeepSeek 差异）

**问题**:
- ❌ 无 API 调用重试逻辑（瞬态失败立即返回）
- ❌ 每次请求都进行新 API 调用（无缓存）

#### FunctionToolRegistry.java - 函数调用框架
**职责**: 工具发现和执行

**优势**:
- ✅ 工具发现的注册表模式
- ✅ 从数据库动态加载工具定义（function_tool 表）
- ✅ 遗留模式规范化（将示例值转换为 JSON Schema）

**问题**:
- ❌ 硬编码执行器映射（添加工具需要代码更改）
- ❌ 工具和执行器之间紧密耦合

**当前设计（紧密耦合）**:
```java
public FunctionToolRegistry(...) {
    // 硬编码映射
    this.executors.put("searchProducts", args -> toolExecutors.searchProducts(...));
    this.executors.put("getProductDetail", args -> toolExecutors.getProductDetail(...));
    // 添加新工具 = 代码更改
}
```

**推荐设计（可扩展）**:
```java
// 使用 Spring 注解进行自动发现
@Component
@FunctionTool(name = "searchProducts", schema = "...")
public class SearchProductsTool implements ToolExecutor {
    public String execute(Map<String, Object> args, Long userId) {
        // 实现
    }
}
```

#### RagService.java - 知识检索服务
**优势**:
- ✅ 语义分块，重叠以保持上下文连续性
- ✅ 产品范围知识过滤
- ✅ 混合方法：向量搜索 + 新文档的完整文档回退
- ✅ 知识库更新的重建索引能力

**问题**:
- ❌ 固定 420 字符分块（可能不适合所有内容类型）
- ❌ 未记录分块策略

#### ToolExecutors.java - 工具实现层
**实现的工具**: searchProducts、getProductDetail、getUserProfile、getSimilarProducts、getOrderStatus、getAfterSaleRule、submitRecommendation

**严重架构违规**:
```java
public class ToolExecutors {
    // ❌ 直接注入 Mapper（绕过服务层）
    @Autowired private ProductMapper productMapper;
    @Autowired private OrderMapper orderMapper;
    @Autowired private ProductFavoriteMapper favoriteMapper;
    
    public String searchProducts(...) {
        // ❌ 直接调用 mapper
        List<Product> products = productMapper.search(...);
    }
}
```

**应该是**:
```java
public class ToolExecutors {
    @Autowired private ProductService productService;
    @Autowired private OrderService orderService;
    
    public String searchProducts(...) {
        List<Product> products = productService.search(...);
    }
}
```

**影响**:
- 重复业务逻辑
- 绕过验证和事务边界
- 违反分层架构原则

#### VectorStore.java - 混合检索实现
**优势**:
- ✅ 向量搜索的余弦相似度
- ✅ 使用 CJK 二元组分词的词法匹配
- ✅ 可配置的混合评分（78% 向量，22% 词法，8% 重排加成）

**问题**:
- ❌ 加载所有分块到内存进行过滤（可扩展性问题）
- ❌ 权重未文档化或可调
- ❌ 相似度阈值硬编码

**可扩展性问题**:
```java
// 当前：加载所有分块
List<KnowledgeChunk> chunks = chunkMapper.findAll();  // ❌ 内存问题

// 推荐：分页或数据库级过滤
List<KnowledgeChunk> chunks = chunkMapper.findByProductIdWithPagination(productId, page);
```

### 1.2 AI 层架构图

```
┌─────────────────────────────────────────────┐
│         AiController (REST API)              │
└──────────────┬──────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────┐
│         AiChatService (路由层)              │
│  ┌─────────┬──────────┬──────────┬────────┐ │
│  │ GUIDE   │ PRODUCT_ │ PRICE_   │ ORDER_ │ │
│  │         │ QA       │ STOCK    │ STATUS │ │
│  └────┬────┴────┬─────┴────┬─────┴───┬────┘ │
└───────┼─────────┼──────────┼─────────┼──────┘
        │         │          │         │
        ▼         ▼          ▼         ▼
┌───────────┐ ┌──────────┐ ┌──────┐ ┌──────┐
│AgentEngine│ │RagService│ │直接DB│ │直接DB│
└─────┬─────┘ └──────────┘ └──────┘ └──────┘
      │
      ├─► FunctionToolRegistry
      │   └─► ToolExecutors ❌ → Mappers (违反分层)
      │                       应该 → Services
      │
      └─► DeepSeekClient → OpenAI API
```

---

## 2. 领域模型边界

### 2.1 领域分析

#### 商品域 ✓ 良好隔离
**实体**: Product, Category, Brand, ProductImage, ProductFavorite  
**边界**: 清晰、自包含  
**依赖**: 无（仅被其他域引用）  
**评估**: ✅ 优秀

#### 订单域 ⚠️ 良好但有耦合
**实体**: Order, OrderItem, CartItem  
**边界**: 明确定义的事务范围  
**依赖**: 引用 Product（价格/库存快照）、Address、User  
**问题**: OrderService 依赖 WalletService（跨域耦合）  
**评估**: ⚠️ 良好但需要改进

#### 用户域 ⚠️ 与财务混合
**实体**: User, Address, Wallet, WalletRecharge  
**边界**: 混合 - Wallet 感觉像独立的财务域  
**依赖**: 独立  
**建议**: 将 Wallet 实体提取到独立包  
**评估**: ⚠️ 需要拆分

#### AI 域 ❌ 过于宽泛
**实体**: AgentRun, AgentStep, FunctionTool, FunctionCallLog, GuideTask, RecommendResult, KnowledgeDoc, KnowledgeChunk, PromptTemplate, HighFreqQuestion, AfterSaleRule, EvaluationAnalysis, ModelConfig, OperationReport

**问题**: 14 个实体涵盖不同关注点  
**应该拆分为**:
- **ai.agent**: AgentRun, AgentStep, FunctionCallLog
- **ai.knowledge**: KnowledgeDoc, KnowledgeChunk
- **ai.config**: ModelConfig, PromptTemplate, FunctionTool
- **ai.analytics**: HighFreqQuestion, EvaluationAnalysis, OperationReport

**评估**: ❌ 需要重构

### 2.2 边界问题

**贫血模型**:
- 所有实体都是没有行为的 DTO
- 业务逻辑在服务层，而不是域模型

**缺少聚合**:
- Order → OrderItem 关系未在域级别强制执行
- 无聚合根模式

**无领域事件**:
- 订单状态更改不发布事件用于 AI 分析
- 导致直接查询而不是事件驱动集成

**跨域导航**:
- ToolExecutors 跨所有域查询
- 无清晰的有界上下文

---

## 3. 分层架构

### 3.1 层结构

```
┌────────────────────────────────────────┐
│   Controller Layer (11 controllers)    │  REST API
├────────────────────────────────────────┤
│   Service Layer (9 + AI services)      │  业务逻辑
├────────────────────────────────────────┤
│   Mapper Layer (MyBatis)                │  数据访问
├────────────────────────────────────────┤
│   Database (MySQL)                      │  持久化
└────────────────────────────────────────┘
```

### 3.2 分层违规

#### 主要违规 #1: AI 层绕过服务层
```
❌ 当前:
ToolExecutors → ProductMapper (绕过 ProductService)
ToolExecutors → OrderMapper (绕过 OrderService)

✅ 应该:
ToolExecutors → ProductService → ProductMapper
ToolExecutors → OrderService → OrderMapper
```

**影响**:
- 重复业务逻辑
- 绕过验证和事务边界
- 违反单一职责原则

#### 主要违规 #2: Controller 暴露 Mapper
```java
// AiController.java
@Autowired private AgentRunMapper agentRunMapper;
@Autowired private AgentStepMapper agentStepMapper;
@Autowired private GuideTaskMapper guideTaskMapper;

// ❌ Controller 直接使用 Mapper
@GetMapping("/agent/runs")
public Result<List<AgentRun>> getRuns() {
    return Result.success(agentRunMapper.findByUserId(...));
}
```

**应该**:
```java
@Autowired private AiChatService aiChatService;

@GetMapping("/agent/runs")
public Result<List<AgentRun>> getRuns() {
    return Result.success(aiChatService.getAgentRuns());
}
```

#### 次要问题: 服务交叉依赖
```java
// OrderService.java
@Autowired private WalletService walletService;  // 可接受（如果同一有界上下文）
```

### 3.3 依赖流

**预期**: Controller → Service → Mapper → DB

**实际**:
- ProductController → ProductService → ProductMapper ✓
- OrderController → OrderService → OrderMapper + WalletService ✓
- AiController → AiChatService + AgentEngine + Mappers ✗ (混合层)
- ToolExecutors → Mappers ✗ (跳过服务层)

### 3.4 优势

✅ 清晰的包结构（controller、service、mapper、entity）  
✅ 一致的命名约定  
✅ 正确使用 Spring 依赖注入  
✅ 服务层的事务管理  
✅ AI 包与核心业务逻辑分离

---

## 4. 修复建议

### 4.1 立即修复（高优先级）

#### 1. 重构 ToolExecutors 使用服务层
**当前**:
```java
public class ToolExecutors {
    @Autowired private ProductMapper productMapper;
    
    public String searchProducts(...) {
        List<Product> products = productMapper.search(...);
    }
}
```

**修复后**:
```java
public class ToolExecutors {
    @Autowired private ProductService productService;
    
    public String searchProducts(..., Long userId) {
        // 服务层处理权限和业务逻辑
        List<Product> products = productService.search(...);
    }
}
```

#### 2. 从 AiController 提取服务
创建 `AgentHistoryService` 用于 run/step 查询：
```java
@Service
public class AgentHistoryService {
    @Autowired private AgentRunMapper agentRunMapper;
    
    public List<AgentRun> getUserRuns(Long userId) {
        return agentRunMapper.findByUserId(userId);
    }
}
```

#### 3. 解耦工具注册
使用 Spring 注解进行自动发现：
```java
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FunctionTool {
    String name();
    String schema();
}

// 自动发现所有 @FunctionTool 组件
Map<String, ToolExecutor> tools = 
    applicationContext.getBeansWithAnnotation(FunctionTool.class);
```

#### 4. 添加 VectorStore 分页
避免大型知识库的内存问题：
```java
// 实现基于游标的加载
public List<KnowledgeChunk> search(String query, int limit) {
    // 数据库级过滤和分页
    return chunkMapper.searchWithPagination(query, limit);
}
```

### 4.2 中期修复

#### 5. 拆分 AI 域
```
com.aimall.ai/
├── agent/          # AgentRun, AgentStep, FunctionCallLog
├── knowledge/      # KnowledgeDoc, KnowledgeChunk
├── config/         # ModelConfig, PromptTemplate, FunctionTool
└── analytics/      # HighFreqQuestion, EvaluationAnalysis
```

#### 6. 为 API 契约引入 DTO
防止实体变更破坏 API：
```java
// API DTO
public class ProductDTO {
    private Long id;
    private String name;
    private BigDecimal price;
    // 不暴露内部字段
}

// 转换器
public class ProductConverter {
    public static ProductDTO toDTO(Product entity) {
        // ...
    }
}
```

#### 7. 添加缓存
```java
@Cacheable("promptTemplates")
public PromptTemplate getTemplate(String type) {
    return promptTemplateMapper.findByType(type);
}

@Cacheable(value = "products", key = "#productId")
public Product getProductDetail(Long productId) {
    return productMapper.findById(productId);
}
```

#### 8. 提取财务子域
```
com.aimall.finance/
├── entity/         # Wallet, WalletRecharge, Transaction
├── service/        # WalletService, TransactionService
└── mapper/         # WalletMapper, TransactionMapper
```

### 4.3 长期修复（架构）

#### 9. 领域驱动设计重构
引入聚合根：
```java
@Entity
public class Order {  // 聚合根
    private List<OrderItem> items;
    
    // 域行为
    public void addItem(Product product, int quantity) {
        // 业务规则
        if (product.getStock() < quantity) {
            throw new InsufficientStockException();
        }
        items.add(new OrderItem(product, quantity));
    }
}
```

#### 10. 事件驱动架构
发布领域事件：
```java
@Service
public class OrderService {
    @Autowired private ApplicationEventPublisher eventPublisher;
    
    public void payOrder(String orderNo) {
        Order order = pay(orderNo);
        eventPublisher.publishEvent(new OrderPaidEvent(order));
    }
}

@EventListener
public void onOrderPaid(OrderPaidEvent event) {
    // AI 分析订阅事件而不是直接查询
    analysisService.analyzeOrder(event.getOrder());
}
```

#### 11. AI 查询的 CQRS
为 agent 历史和推荐分离读模型：
```java
// 写模型
@Entity
public class AgentRun { ... }

// 读模型
@Entity
@Immutable
public class AgentRunSummary {
    private Long runId;
    private Integer stepCount;
    private String result;
    // 非规范化用于快速查询
}
```

#### 12. 工具执行隔离
添加资源限制的沙盒：
```java
public interface ToolExecutor {
    @Timeout(5000)  // 5 秒超时
    @RateLimit(10)  // 每秒 10 次调用
    String execute(Map<String, Object> args, Long userId);
}
```

---

## 5. 架构评分卡

| 维度 | 评分 | 评论 |
|------|------|------|
| **AI 层设计** | 8/10 | 优秀的职责分离，需要权限修复 |
| **领域边界** | 5/10 | AI 域过大，需要拆分 |
| **分层架构** | 5/10 | 主要违规（绕过服务层） |
| **可扩展性** | 6/10 | 工具注册硬编码，VectorStore 内存问题 |
| **可维护性** | 6/10 | 需要更好的分离和 DTO |
| **可观测性** | 8/10 | 优秀的 agent 步骤日志 |
| **整体** | **6.5/10** | 坚实的基础，关键重构需要 |

---

## 6. 总结

**优势**:
- 清晰的 AI 层设计，职责分离良好
- 灵活的多提供商 LLM 支持
- 强大的 agent 执行可观测性
- 核心业务逻辑的标准 Spring Boot 分层

**关键问题**:
- AI 层绕过服务层（违反分层）
- Controller 直接使用 mapper（破坏封装）
- AI 域过于宽泛（需要子域拆分）
- 工具注册硬编码（可扩展性问题）

**领域成熟度**:
- 商品、订单域：结构良好
- 用户域：与财务关注混合
- AI 域：需要分解

架构**可投入生产**但需要重构以实现长期可维护性，特别是在 AI-业务逻辑集成点。
