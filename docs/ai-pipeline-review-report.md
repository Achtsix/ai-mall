# AI 决策链路审查报告

## 审查概述

本报告对 AI Mall 项目的 AI 决策链路进行了深度审查，涵盖以下核心文件：
- AgentEngine.java
- FunctionToolRegistry.java
- ToolExecutors.java
- RagService.java
- VectorStore.java
- DeepSeekClient.java

## 关键发现

### P0 - 严重安全问题

#### 1. 工具执行器缺少用户权限验证
**文件**: `ToolExecutors.java`  
**位置**: 
- `getUserProfile()` (第 87-115 行)
- `getOrderStatus()` (第 154-168 行)

**问题描述**:
- `getUserProfile()` 接受任意 `userId` 参数，未验证请求用户是否有权限查看该用户资料
- `getOrderStatus()` 接受任意 `orderNo` 参数，未验证订单是否属于请求用户

**影响**: 用户 A 可以通过操纵工具参数查询用户 B 的订单、偏好和购买历史

**修复建议**:
```java
// 在 ToolExecutors 中添加权限验证
public String getUserProfile(Long userId) {
    Long currentUserId = UserContext.getUserId();
    if (!currentUserId.equals(userId)) {
        throw new BusinessException("无权访问其他用户的资料");
    }
    // 继续执行...
}
```

#### 2. 用户上下文未传递到工具层
**文件**: `AgentEngine.java`  
**问题**: `AgentEngine.run()` 接受 `userId` 但从未传递给工具执行器进行权限检查  
**影响**: 工具无法验证"该用户是否允许查看此数据"

### P1 - 高优先级问题

#### 3. 推荐验证不完整
**文件**: `AgentEngine.java` (第 209-233 行)  
**方法**: `saveRecommendation()`

**问题**:
- 验证商品是否存在，但未验证：
  - 当前价格是否与模型在工具调用时"看到"的价格一致
  - 库存在工具调用和推荐之间是否发生变化

**风险**: 模型可能基于对话早期的过时数据进行推荐

#### 4. 最终答案未记录为 Agent Step
**问题**: 仅为工具调用记录步骤（第 147-155 行），最终 LLM 响应未记录  
**影响**: 无法审计模型说了什么 vs. 工具返回了什么

#### 5. 错误处理静默失败
**文件**: `AgentEngine.java` (第 230-232 行)  
**问题**: `saveRecommendation()` 捕获所有异常并标记为 `ignored`  
**影响**: 如果推荐保存失败，用户收到成功消息但没有存储结果，且无日志或告警

### P2 - 中等优先级问题

#### 6. 商品数据并发访问
**文件**: `AgentEngine.java` (第 217 行)  
**问题**: `saveRecommendation()` 中的商品查询不是事务性的  
**风险**: 竞态条件，商品可能在检查和保存之间被删除/修改

#### 7. RAG 相似度阈值硬编码
**文件**: `VectorStore.java` (第 31 行)  
**问题**: 
- 相关性过滤无可配置阈值
- 低质量匹配总是包含在 top-K 结果中
- 权重（0.78 向量，0.22 词法）未文档化或可调

#### 8. 无限流或滥用防护
**文件**: `AgentEngine.java`  
**问题**:
- `AgentEngine.run()` 无每用户限流
- 用户可以生成无限并发运行
- `maxSteps=8` 防止无限循环但不防止成本滥用

#### 9. 工具执行异常丢失上下文
**文件**: `AgentEngine.java` (第 140-144 行)  
**问题**: 通用错误消息不保留堆栈跟踪  
**影响**: 使生产问题调试困难

### P3 - 低优先级/技术债务

#### 10. AI API 失败无重试逻辑
**文件**: `DeepSeekClient.java` (第 108-128 行)  
**问题**: 瞬态网络错误立即失败，429/503 响应无指数退避

#### 11. Agent Step 序列非原子
**文件**: `AgentEngine.java` (第 149 行)  
**问题**: 使用循环计数器作为 `seq`，如果步骤失败可能跳过数字  
**建议**: 应使用数据库序列或小心递增

#### 12. 历史注入未验证
**文件**: `AgentEngine.java` (第 96-107 行)  
**问题**: 接受任意 `history` 参数而不进行模式验证  
**风险**: 可通过角色操纵注入恶意系统提示

#### 13. Embedding 降级静默
**文件**: `DeepSeekClient.java` (第 165 行)  
**问题**: 降级到本地 256 维 embedding 而不告警  
**影响**: 可能导致检索质量下降而未被注意

#### 14. 工具调用结果无大小限制
**问题**: `searchProducts` 返回最多 20 个商品（第 53 行）  
**风险**: 大型结果可能在长对话中超出 LLM 上下文窗口

## 架构优势

- Agent 步骤和函数调用正确记录到数据库
- 混合检索（向量 + 词法）提高精确匹配准确性
- 快照字段在推荐时捕获价格/库存
- 系统正确处理工具错误并继续执行

## 架构劣势

- Agent 和数据访问之间无授权层
- 验证发生在事后（快照）而非防止过时推荐
- 错误边界过宽（使用 `ignored` 的 catch-all）
- 最终模型响应质量无可观测性

## 修复优先级

**立即修复 (P0)**:
1. 为所有工具执行器添加 `requestingUserId` 参数
2. 在 `getUserProfile()` 和 `getOrderStatus()` 中验证用户所有权
3. 使用清晰错误消息拒绝跨用户数据访问

**短期修复 (P1)**:
4. 将最终答案记录为 agent step
5. 在 `submitRecommendation` 中实现实时验证 - 将工具调用结果与当前数据库状态比较
6. 将 `ignored` 异常替换为适当的日志记录和面向用户的错误

**中期修复 (P2)**:
7. 使 RAG 相似度阈值可配置
8. 为推荐保存添加事务保证
9. 在 AgentEngine 级别实现每用户限流

**最关键的问题是缺少权限验证 - 这应该在任何生产部署之前修复。**
