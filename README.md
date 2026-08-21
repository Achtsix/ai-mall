# AI 智能导购商城（Spring Boot 3 + Vue 3 + Agent + RAG + Function Calling + Embedding）

> 在传统商城之上叠加 AI 导购 Agent：RAG 商品知识库 + Function Calling 工具中心 + Agent Run/Step 自主决策 + 多轮导购对话 + AI 评价分析与运营增长报告。

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

### 2. 修改配置

编辑 `backend/src/main/resources/application.yml`：

- MySQL 用户名和密码
- `DEEPSEEK_API_KEY`（或环境变量 `DEEPSEEK_API_KEY`）

如需接入 OpenAI 兼容接口（默认模型名为 `gpt-5.6`），启动前设置：

```bat
set OPENAI_API_KEY=你的 API Key
set OPENAI_BASE_URL=https://api.openai.com/v1
set OPENAI_MODEL=gpt-5.6
```

也可以使用你所在服务商提供的 OpenAI 兼容 Base URL；API Key 只放在后端环境变量中，不要放入前端。

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
