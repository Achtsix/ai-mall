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

## 已验证指标

项目包含可重复运行的独立基准测试，测试库固定为 `ai_mall_benchmark`，不会清理或改写展示库 `ai_mall`。2026-08-21 的固定数据集结果如下：

- 27 张业务表、10 个用户端路由、20 个基准商品、20 份知识文档；
- 商品接口支持关键词、分类、品牌、最低价、最高价 5 类查询条件；40 条搜索用例的 nDCG@5 为 0.63；
- 混合 RAG 在 40 条检索用例上的 nDCG@5 为 0.86，相比纯向量的 0.79 提升 9.36%；
- JWT 访问矩阵 6/6 通过，2000 次并发交错身份请求中串号和请求错误均为 0；
- 7 个 Function Tool 均具有合法 JSON Schema；26 项价格/库存事实核对错误为 0；
- 18/18 个在线问答场景成功，10/10 个 Agent 场景完成；Agent 延迟中位数 12.219 秒、P95 17.073 秒；
- Playwright 覆盖用户购物与余额支付、管理员入口和移动端导航，3/3 条端到端流程通过。

详细口径、失败案例和运行方式见 [量化测试报告](docs/benchmark-report.md)，可用于简历的事实版描述见 [项目介绍与个人优势](docs/resume-project-description.md)。原始结果保存在 `docs/benchmark-results.json`。

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
