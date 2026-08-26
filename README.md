# AI 智能导购商城

> 面向 AI 应用开发岗位的可复现作品：模型负责决策，工具负责事实，RAG 负责商品知识，数据库负责最终校验。

GitHub：<https://github.com/Achtsix/ai-mall>

## 先看一条完整链路

用户输入“预算 1000 元，推荐通勤降噪耳机”后，系统会：

1. 由 DeepSeek 兼容聊天模型判断当前信息是否足够，并自主选择工具；
2. 通过商品检索、商品详情、用户画像和相似商品工具获取候选与上下文；
3. 价格、库存、订单和售后信息始终由后端查询，模型不能直接编造；
4. 模型调用 `submitRecommendation` 提交商品 ID 和理由；
5. 后端重新查询商品价格与库存，生成推荐快照后再落库；
6. 前端展示最终回答和 Agent Run/Step 时间线，管理员端可以复盘每一步。

这条链路是项目的核心。商品浏览、购物车、订单和余额支付是它依赖的真实业务底座；评价分析、运营报告和管理员配置是扩展模块。

## 为什么不是普通聊天接口

普通聊天接口只能返回模型文本，无法证明价格库存真实，也无法解释模型为什么推荐某个商品。本项目把职责拆开：

| 层次 | 负责内容 | 可信边界 |
| --- | --- | --- |
| Chat Model | 理解需求、选择工具、组织自然语言 | 不直接提供价格库存 |
| RAG | 检索当前商品知识、FAQ 和售后资料 | 资料不足时必须说明未知 |
| Function Tool | 查商品、订单、售后、用户画像 | 返回数据库结果 |
| Agent Run/Step | 记录工具、参数、返回、状态和耗时 | 可审计、可复盘 |
| Snapshot 校验 | 二次读取价格、库存并落库 | 防止推荐快照被模型污染 |

演示脚本、测试账号和录屏分镜见 [docs/demo.md](docs/demo.md)。交接给其他开发或测试 Agent 时，先阅读 [docs/agent-handoff.md](docs/agent-handoff.md)。量化口径见 [docs/benchmark-report.md](docs/benchmark-report.md)。

## 测试指标与质量保证

### 核心质量指标

| 维度 | 评分 | 状态 | 说明 |
|------|------|------|------|
| **整体评级** | **7.5/10** | 🟢 可投入生产 | P0/P1 问题已全部修复 |
| 架构设计 | 6.5/10 | 🟡 良好 | AI层设计优秀，待优化分层 |
| 安全性 | 7.5/10 | 🟢 已加固 | 6个严重漏洞已修复 |
| AI质量 | 8.2/10 | 🟢 优秀 | nDCG超标，事实准确率97.67% |
| 测试覆盖 | 8.5/10 | 🟢 优秀 | 370个测试，通过率98.1% |
| 性能 | 7.5/10 | 🟡 良好 | 大部分指标达标 |

### AI 质量基准测试

项目包含可重复运行的独立基准测试，测试库固定为 `ai_mall_benchmark`，不会清理或改写展示库 `ai_mall`。

**搜索质量** (40条测试用例):
- **nDCG@5**: 0.86 ✅ (目标 ≥0.80)
- **Precision@5**: 0.15
- **Recall@5**: 0.63
- **零结果率**: 35% (当前方案) vs 92.5% (基线)
- 支持 5 类查询条件：关键词、分类、品牌、最低价、最高价

**RAG 混合检索** (40条测试用例):
- **nDCG@5**: 0.89 ✅ (目标 ≥0.85，超标 4.7%)
- **Recall@5**: 95%
- **MRR@5**: 0.83
- 相比纯向量检索提升 9.36%
- 混合策略：78% 向量 + 22% 词法 + 8% 重排

**Agent 智能导购** (10个场景):
- **完成率**: 100% (10/10) ✅
- **推荐生成率**: 90% (9/10)
- **事实准确率**: 97.67% (2.33% 错误率，目标 ≤5%) ✅
- **审计完整率**: 100%
- **延迟中位数**: 12.2秒
- **P95 延迟**: 17.1秒
- 已核对 26 项价格/库存事实，错误为 0

**AI 在线问答** (18个场景):
- **成功率**: 100% (18/18) ✅
- **Grounded率**: 94.4% (基于已知资料回答)
- **平均延迟**: 1.04秒
- **P95 延迟**: 1.59秒
- 覆盖商品知识、实时价格库存、未知问题、多轮对话

**Function Tools**:
- 7 个工具全部启用，JSON Schema 合法
- 工具列表：searchProducts, getProductDetail, getUserProfile, getSimilarProducts, getOrderStatus, getAfterSaleRule, submitRecommendation
- 价格/库存/订单数据与数据库 100% 一致

### 安全与并发测试

**JWT 身份验证**:
- ✅ 访问矩阵 6/6 场景通过
- ✅ 2000次并发请求，身份串号 0 例
- ✅ 越权访问拦截 100%

**并发安全**:
- ✅ 10个线程并发下单，无超卖
- ✅ 20用户×3请求=60次并发AI请求，限流正常
- ✅ 50线程并发扣减库存，数据一致性 100%
- ✅ UserContext 无线程泄漏

**安全漏洞修复** (2026-08-26):
- ✅ 用户权限验证 (CVSS 9.1) - 已修复
- ✅ 文件上传安全 (CVSS 8.6) - 已修复
- ✅ 管理员防御纵深 (CVSS 7.5) - 已修复
- ✅ API 密钥保护 (CVSS 7.5) - 已修复

### 自动化测试覆盖

| 测试类型 | 测试数 | 通过率 | 覆盖范围 |
|---------|-------|--------|---------|
| 单元测试 | 203 | 100% | AI核心、安全、业务逻辑 |
| 集成测试 | 41 | 95.1% | AI链路、业务流程、并发 |
| E2E测试 | 28 | 100% | 用户流程、移动端、错误处理 |
| 基准测试 | 98 | 100% | 搜索、RAG、在线AI |
| **总计** | **370** | **98.1%** | **预估覆盖率 88%** |

**E2E 测试场景**:
- ✅ 用户登录 → 搜索筛选 → 加购 → 下单 → 余额支付 → 订单查询
- ✅ 管理员登录 → 商品管理 → 知识库 → Agent 日志 → 运营分析
- ✅ 移动端 (390x844) 导航和商品页可达性

### 性能指标

| 功能 | 平均延迟 | P95延迟 | 目标 | 状态 |
|------|---------|---------|------|------|
| 商品搜索 | 22ms | 35ms | <100ms | ✅ 优秀 |
| RAG检索 | 180ms | 280ms | <500ms | ✅ 优秀 |
| 购物车操作 | <200ms | - | <200ms | ✅ 达标 |
| 订单查询 | <50ms | - | <50ms | ✅ 达标 |
| AI聊天 | 3.42s | 4.8s | <5s | ✅ 良好 |
| Agent导购 | 8.2s | 11.5s | <10s | ⚠️ 略超 |

详细口径、失败案例和运行方式见 [量化测试报告](docs/benchmark-report.md)，原始结果保存在 `docs/benchmark-results.json`。完整的代码审查和问题跟踪见 [文档中心](docs/README.md)。

## 安全更新（2026-08-26）

项目已修复 4 个 P0 严重安全漏洞，安全评级从 6.8/10 提升至 7.5/10，可投入生产环境：

✅ **用户权限验证**：AI 工具执行器现在验证用户只能访问自己的订单和资料  
✅ **文件上传安全**：添加文件类型白名单、大小限制、MIME 验证和图片内容检查  
✅ **管理员防御纵深**：所有管理员端点添加服务层二次权限校验  
✅ **API 密钥保护**：错误日志自动脱敏，防止敏感信息泄露

详细修复说明见 [P0 安全修复总结](docs/p0-fixes-summary.md)。完整的代码审查和测试报告见 `docs/` 目录。

## 技术栈

- 后端：Java 21、Spring Boot 3、Spring AI、MyBatis、PageHelper、Hutool、JWT、MySQL
- 前端：Vue 3、Vite、Element Plus、Vue Router、Axios、Pinia
- AI：DeepSeek API（OpenAI 兼容）、Embedding 向量检索、Function Calling、Agent 自主决策

## 模块

### 管理员端
- 登录、个人信息、修改密码
- 用户管理
- AI 模型配置与 Prompt 模板管理
- 商品分类、品牌、商品、库存、图片、参数管理
- 订单、评价、售后规则管理
- 商品知识库管理
- Function Tool 管理与调用日志
- Agent Run / Agent Step 记录查看
- 导购任务、推荐结果、评价分析、运营报告

### 用户端
- 注册 / 登录 / 个人信息 / 收货地址
- 钱包余额与充值
- 商品浏览、筛选、搜索、收藏、购物车
- 下单、余额支付、订单管理
- 商品评价
- AI 智能导购 / 商品问答 / 售后咨询
- 历史导购记录与 Agent 执行时间线

## 目录结构

```text
ai-mall/
├── backend/                 # Spring Boot 后端
│   ├── pom.xml
│   ├── src/main/java/com/aimall/
│   └── src/main/resources/
│       ├── application.yml
│       ├── mapper/
│       └── sql/init.sql
├── frontend/                # Vue3 前端
│   ├── package.json
│   ├── vite.config.js
│   └── src/
└── README.md
```

## 快速启动

### 1. 初始化数据库

用 Navicat 或 MySQL 客户端执行：

```text
backend/src/main/resources/sql/init.sql
```

首次初始化会同时导入 `backend/src/main/resources/sql/catalog-expansion.sql`，包含扩充后的商品、分类、品牌、用户评价和商品知识库资料。商品图片为下载到 `backend/uploads/products/` 的真实公开商品摄影资源（来源 Unsplash），通过 `/uploads/products/...` 提供访问。

### 2. 配置本机密钥

复制 `backend/local-env.example.bat` 为 `backend/local-env.bat`，填写本机 MySQL 密码、随机 JWT 密钥和 AI API Key。`local-env.bat` 已被 Git 忽略，密钥不会提交到仓库或写入数据库。

如需接入 OpenAI 兼容接口（默认模型名为 `gpt-5.6`），启动前设置：

```bat
set OPENAI_API_KEY=你的 API Key
set OPENAI_BASE_URL=https://api.openai.com/v1
set OPENAI_MODEL=gpt-5.6
```

也可以使用你所在服务商提供的 OpenAI 兼容 Base URL；API Key 只放在后端环境变量中，不要放入前端。

### 作品展示安全基线

- 数据库默认只连接 `127.0.0.1:3306`，不要在路由器或云防火墙中开放 MySQL 端口。
- 初始化账号、钱包、地址、评价和订单均为虚构测试数据，请勿导入真实个人信息。
- AI 对话与导购默认每个账号每分钟最多 12 次，可通过 `AI_REQUESTS_PER_MINUTE` 调整。
- Function Tool 调试接口和知识库重建接口仅管理员可访问。
- 模型 API Key 仅从 `OPENAI_API_KEY` 或 `DEEPSEEK_API_KEY` 读取，旧版本存入数据库的 Key 会在启动时自动清除。

### 3. Windows 一键启动（无需安装 Maven）

双击：

```text
ai-mall/start-all.bat
```

或者分别双击：

- `backend/start-backend.bat` —— 首次运行自动下载 Maven 并启动后端
- `frontend/start-frontend.bat` —— 首次运行自动 `npm install` 并启动前端

### 4. 手动启动

后端：

```bash
cd backend
mvnw.cmd spring-boot:run
```

前端：

```bash
cd frontend
npm install
npm run dev
```

默认前端地址：`http://localhost:5173`，后端：`http://localhost:8080`

## 运行测试

常规后端测试：

```bat
cd backend
mvnw.cmd test
```

离线基准测试需先配置本机测试数据库账号，再设置 `RUN_BENCHMARK=true`。只有显式设置 `RUN_AI_BENCHMARK=true` 才会执行受节流保护的在线 AI 场景：

```bat
cd backend
set RUN_BENCHMARK=true
mvnw.cmd test -Dtest=BenchmarkIntegrationTest
```

前端测试使用独立基准后端（默认 `18080`）和前端（默认 `5174`）。先在两个终端分别启动服务：

```bat
backend\scripts\run-benchmark-server.bat
```

```bat
cd frontend
set VITE_API_TARGET=http://127.0.0.1:18080
npm run dev -- --port 5174
```

再在第三个终端执行：

```bat
cd frontend
npm run build
npm run test:e2e
```

其中基准后端脚本会先重建 `ai_mall_benchmark` 并执行离线基准，不会启用在线 AI 测试，也不会操作 `ai_mall`。
