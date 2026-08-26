-- ============================================================
-- AI 智能导购商城初始化 SQL
-- 适用于 MySQL 5.7+ / 8.x
-- ============================================================
CREATE DATABASE IF NOT EXISTS ai_mall DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ai_mall;

-- 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL COMMENT 'BCrypt/ Hutool BCrypt 加密',
  nickname VARCHAR(50),
  avatar VARCHAR(255),
  phone VARCHAR(20),
  email VARCHAR(100),
  role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'ADMIN / USER',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1正常 0禁用',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 收货地址
DROP TABLE IF EXISTS user_address;
CREATE TABLE user_address (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  receiver_name VARCHAR(50) NOT NULL,
  receiver_phone VARCHAR(20) NOT NULL,
  province VARCHAR(50),
  city VARCHAR(50),
  district VARCHAR(50),
  detail VARCHAR(255) NOT NULL,
  is_default TINYINT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 钱包
DROP TABLE IF EXISTS wallet;
CREATE TABLE wallet (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL UNIQUE,
  balance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 钱包充值记录
DROP TABLE IF EXISTS wallet_recharge;
CREATE TABLE wallet_recharge (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  balance_after DECIMAL(12,2),
  status TINYINT DEFAULT 1 COMMENT '1成功',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 商品分类
DROP TABLE IF EXISTS category;
CREATE TABLE category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id BIGINT DEFAULT 0,
  name VARCHAR(50) NOT NULL,
  sort INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 品牌
DROP TABLE IF EXISTS brand;
CREATE TABLE brand (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  logo VARCHAR(255),
  description VARCHAR(500),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 商品
DROP TABLE IF EXISTS product;
CREATE TABLE product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  category_id BIGINT NOT NULL,
  brand_id BIGINT,
  name VARCHAR(200) NOT NULL,
  subtitle VARCHAR(500),
  main_image VARCHAR(500),
  price DECIMAL(12,2) NOT NULL,
  original_price DECIMAL(12,2),
  stock INT NOT NULL DEFAULT 0,
  sales INT NOT NULL DEFAULT 0,
  status TINYINT NOT NULL DEFAULT 1 COMMENT '1上架 0下架',
  detail_html MEDIUMTEXT,
  params_json TEXT COMMENT '规格参数 JSON',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_category (category_id),
  KEY idx_brand (brand_id),
  KEY idx_name (name)
);

-- 商品图片
DROP TABLE IF EXISTS product_image;
CREATE TABLE product_image (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  url VARCHAR(500) NOT NULL,
  sort INT DEFAULT 0,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 商品收藏
DROP TABLE IF EXISTS product_favorite;
CREATE TABLE product_favorite (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_product (user_id, product_id)
);

-- 购物车
DROP TABLE IF EXISTS cart_item;
CREATE TABLE cart_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL DEFAULT 1,
  checked TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_product (user_id, product_id)
);

-- 订单
DROP TABLE IF EXISTS orders;
CREATE TABLE orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(40) NOT NULL UNIQUE,
  user_id BIGINT NOT NULL,
  total_amount DECIMAL(12,2) NOT NULL,
  pay_amount DECIMAL(12,2) NOT NULL,
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0待支付 1已支付 2已发货 3已完成 4已取消',
  address_snapshot TEXT,
  pay_time DATETIME,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_user (user_id)
);

-- 订单明细
DROP TABLE IF EXISTS order_item;
CREATE TABLE order_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_name VARCHAR(200),
  product_image VARCHAR(500),
  price DECIMAL(12,2) NOT NULL,
  quantity INT NOT NULL,
  total_amount DECIMAL(12,2) NOT NULL
);

-- 商品评价
DROP TABLE IF EXISTS review;
CREATE TABLE review (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  order_id BIGINT,
  rating TINYINT NOT NULL DEFAULT 5,
  content TEXT,
  images TEXT,
  reply TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_product (product_id)
);

-- 售后规则
DROP TABLE IF EXISTS after_sale_rule;
CREATE TABLE after_sale_rule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  category VARCHAR(50),
  keywords VARCHAR(500),
  priority INT DEFAULT 100,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 商品知识库文档
DROP TABLE IF EXISTS knowledge_doc;
CREATE TABLE knowledge_doc (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT,
  title VARCHAR(200) NOT NULL,
  type VARCHAR(50) DEFAULT 'PRODUCT' COMMENT 'PRODUCT / FAQ / AFTER_SALE / REVIEW',
  content MEDIUMTEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 知识库切片 + 向量(JSON 存储)
DROP TABLE IF EXISTS knowledge_chunk;
CREATE TABLE knowledge_chunk (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  doc_id BIGINT NOT NULL,
  content TEXT NOT NULL,
  embedding_json TEXT COMMENT 'Embedding 向量 JSON 数组',
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_doc (doc_id)
);

-- Function Tool 注册表
DROP TABLE IF EXISTS function_tool;
CREATE TABLE function_tool (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL UNIQUE,
  description VARCHAR(1000),
  url VARCHAR(500),
  method VARCHAR(10) DEFAULT 'GET',
  request_schema TEXT,
  response_schema TEXT,
  enabled TINYINT DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 工具调用日志
DROP TABLE IF EXISTS function_call_log;
CREATE TABLE function_call_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  run_id BIGINT,
  step_id BIGINT,
  tool_name VARCHAR(100) NOT NULL,
  input_json TEXT,
  output_json TEXT,
  status VARCHAR(20),
  cost_ms BIGINT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Agent Run
DROP TABLE IF EXISTS agent_run;
CREATE TABLE agent_run (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  question TEXT NOT NULL,
  model VARCHAR(50),
  status VARCHAR(20) DEFAULT 'RUNNING',
  answer MEDIUMTEXT,
  started_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  finished_at DATETIME,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Agent Step
DROP TABLE IF EXISTS agent_step;
CREATE TABLE agent_step (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  run_id BIGINT NOT NULL,
  seq INT NOT NULL,
  tool_name VARCHAR(100),
  input_json TEXT,
  output_json TEXT,
  status VARCHAR(20),
  cost_ms BIGINT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  KEY idx_run (run_id)
);

-- Prompt 模板
DROP TABLE IF EXISTS prompt_template;
CREATE TABLE prompt_template (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  type VARCHAR(50),
  content TEXT NOT NULL,
  enabled TINYINT DEFAULT 1,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 模型配置
DROP TABLE IF EXISTS model_config;
CREATE TABLE model_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  provider VARCHAR(50) DEFAULT 'DEEPSEEK',
  base_url VARCHAR(255),
  api_key VARCHAR(255),
  model VARCHAR(100),
  temperature DECIMAL(3,2) DEFAULT 0.70,
  max_tokens INT DEFAULT 4096,
  enabled TINYINT DEFAULT 1,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 导购任务
DROP TABLE IF EXISTS guide_task;
CREATE TABLE guide_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  question TEXT NOT NULL,
  status VARCHAR(20) DEFAULT 'PROCESSING',
  run_id BIGINT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 推荐结果
DROP TABLE IF EXISTS recommend_result;
CREATE TABLE recommend_result (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  guide_task_id BIGINT,
  run_id BIGINT,
  user_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  reason TEXT,
  price_snapshot DECIMAL(12,2),
  stock_snapshot INT,
  discount_snapshot VARCHAR(255),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 评价分析
DROP TABLE IF EXISTS evaluation_analysis;
CREATE TABLE evaluation_analysis (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT,
  summary TEXT,
  positive_keywords TEXT,
  negative_reasons TEXT,
  after_sale_risks TEXT,
  missing_info TEXT,
  suggestions TEXT,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 运营报告
DROP TABLE IF EXISTS operation_report;
CREATE TABLE operation_report (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(200),
  content MEDIUMTEXT,
  period VARCHAR(50),
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 高频咨询问题统计
DROP TABLE IF EXISTS high_freq_question;
CREATE TABLE high_freq_question (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  question TEXT,
  count INT DEFAULT 1,
  last_ask_time DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 初始化数据
-- ============================================================
-- 以下账号、钱包、评价和订单均为作品展示使用的虚构测试数据，不包含真实个人信息。
-- 默认管理员 / 普通用户，密码均为 123456（仅用于本机演示，请勿用于真实业务）。
INSERT INTO sys_user (id, username, password, nickname, role) VALUES
(1, 'admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '系统管理员', 'ADMIN'),
(2, 'user', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '测试用户', 'USER');

INSERT INTO wallet (user_id, balance) VALUES (1, 0), (2, 1000);

INSERT INTO category (id, parent_id, name, sort) VALUES
(1, 0, '手机数码', 1),
(2, 0, '电脑办公', 2),
(3, 0, '家用电器', 3),
(4, 1, '手机', 1),
(5, 1, '耳机', 2),
(6, 2, '笔记本电脑', 1);

INSERT INTO brand (id, name, description) VALUES
(1, 'DeepTech', 'AI 智能硬件品牌'),
(2, 'CloudBook', '轻薄办公本品牌'),
(3, 'SoundPro', '高品质音频品牌');

INSERT INTO product (id, category_id, brand_id, name, subtitle, main_image, price, original_price, stock, sales, status, detail_html, params_json) VALUES
(1, 4, 1, 'DeepTech X1 智能手机', 'AI 拍照旗舰手机', '/uploads/products/deeptech-x1.jpg', 3999.00, 4599.00, 100, 520, 1, '<p>6.7 英寸 2K 屏幕，AI 影像芯片，5000mAh 长续航。</p>', '{"屏幕":"6.7英寸","内存":"12GB+256GB","电池":"5000mAh"}'),
(2, 5, 3, 'SoundPro Pro 降噪耳机', '主动降噪，Hi-Fi 音质', '/uploads/products/soundpro-pro.jpg', 899.00, 1099.00, 200, 1300, 1, '<p>40dB 深度降噪，30 小时续航，支持无线充电。</p>', '{"降噪":"40dB","续航":"30小时","蓝牙":"5.3"}'),
(3, 6, 2, 'CloudBook Air 14 笔记本', '1.2kg 轻薄本，长续航办公', '/uploads/products/cloudbook-air.jpg', 5299.00, 5999.00, 50, 310, 1, '<p>14 英寸 2.8K 高刷屏，Intel Ultra 5，56Wh 电池。</p>', '{"屏幕":"14英寸 2.8K","重量":"1.2kg","内存":"16GB+1TB"}');

INSERT INTO product_image (product_id, url, sort) VALUES
(1, '/uploads/products/deeptech-x1-2.jpg', 1), (1, '/uploads/products/deeptech-x1-3.jpg', 2),
(2, '/uploads/products/soundpro-pro-2.jpg', 1), (2, '/uploads/products/soundpro-pro-3.jpg', 2),
(3, '/uploads/products/cloudbook-air-2.jpg', 1), (3, '/uploads/products/cloudbook-air-3.jpg', 2);

INSERT INTO after_sale_rule (title, content, category, keywords, priority) VALUES
('七天无理由退货', '自签收次日起 7 天内，商品完好可申请无理由退货。', 'RETURN', '退货,七天,无理由', 1),
('质量问题换货', '签收 15 天内出现非人为质量问题，可申请免费换货。', 'EXCHANGE', '换货,质量,问题', 2),
('保修政策', '整机保修一年，主要部件保修两年。', 'AFTER_SALE', '保修,维修', 3);

INSERT INTO knowledge_doc (product_id, title, type, content) VALUES
(1, 'DeepTech X1 常见问题', 'FAQ', '支持 NFC、双卡双待、IP68 防水。'),
(2, 'SoundPro Pro 使用说明', 'FAQ', '首次使用请充满电，配对时长按耳机功能键 3 秒。'),
(3, 'CloudBook Air 售后说明', 'AFTER_SALE', '全国联保，支持上门维修。');

INSERT INTO function_tool (name, description, url, method, request_schema, response_schema, enabled) VALUES
('searchProducts', '根据关键词、分类、品牌、价格区间搜索商品', '/api/ai/tools/searchProducts', 'POST', '{"keywords":"","categoryId":0,"brandId":0,"minPrice":0,"maxPrice":0}', '{"products":[{"id":1,"name":"","price":0,"stock":0}]}', 1),
('getProductDetail', '获取商品详情、价格、库存、参数、主图', '/api/ai/tools/getProductDetail', 'GET', '{"productId":1}', '{"id":1,"name":"","price":0,"stock":0,"params":{}}', 1),
('getUserProfile', '获取当前用户画像、偏好、历史订单摘要', '/api/ai/tools/getUserProfile', 'GET', '{"userId":1}', '{"userId":1,"preferences":[],"orderCount":0}', 1),
('getSimilarProducts', '获取与指定商品相似的商品', '/api/ai/tools/getSimilarProducts', 'POST', '{"productId":1,"limit":5}', '{"products":[]}', 1),
('getOrderStatus', '查询订单状态', '/api/ai/tools/getOrderStatus', 'GET', '{"orderNo":"20250101001"}', '{"orderNo":"","status":"已支付"}', 1),
('getAfterSaleRule', '查询售后规则', '/api/ai/tools/getAfterSaleRule', 'POST', '{"question":"退货"}', '{"rules":[]}', 1),
('submitRecommendation', '提交最终推荐商品与理由', '/api/ai/tools/submitRecommendation', 'POST', '{"productIds":[1],"reason":"性价比高"}', '{"ok":true,"recommendIds":[1]}', 1);

INSERT INTO prompt_template (name, type, content, enabled) VALUES
('商品导购系统提示词', 'GUIDE', '你是一个专业的电商导购 Agent。

核心原则：
1. 必须通过工具获取真实价格、库存、优惠和用户信息，禁止编造数据
2. 当用户预算超出商品价格范围时，应主动说明实际价格范围，并推荐该品类中的最佳选择
3. 如果搜索结果为空，尝试放宽条件（如提高 maxPrice、移除 brandId）再次搜索
4. 在信息充分后调用 submitRecommendation 提交推荐，并给出可解释的推荐理由

处理预算不匹配的情况：
- 用户说"10000 元耳机"但实际最贵只有 899 元时，应该：
  1. 先搜索该品类（如 categoryId=5 耳机）不限价格
  2. 告知用户"该品类商品价格在 XXX-XXX 元之间，预算 10000 元可以轻松选购旗舰款"
  3. 推荐该品类中评价最好、配置最高的产品
  4. 提供清晰的理由说明为何推荐这些商品

示例场景：
用户："要买 10000 块钱的耳机"
正确回应：
1. 调用 searchProducts({categoryId: 5}) 获取所有耳机
2. 发现价格范围是 299-899 元
3. 回复："目前商城的耳机价格在 299-899 元之间，您 10000 元的预算非常充裕。我为您推荐旗舰款 Sony WH-1000XM6，售价 899 元，具备顶级降噪和 Hi-Res 音质，是该品类的最佳选择。"
4. 调用 submitRecommendation 提交推荐', 1),
('商品问答提示词', 'QA', '你是商品知识助手，请基于检索到的商品知识库内容回答，如果资料不足请明确说明。', 1),
('评价分析提示词', 'REVIEW_ANALYSIS', '你是一位电商运营分析师，请根据评价数据输出好评关键词、差评原因、售后风险点和优化建议。', 1),
('运营报告提示词', 'OPERATION_REPORT', '你是一位增长运营专家，请结合评价分析、高频咨询、导购转化数据生成可执行的运营增长报告。', 1);

INSERT INTO model_config (name, provider, base_url, api_key, model, temperature, max_tokens, enabled) VALUES
('默认 DeepSeek', 'DEEPSEEK', 'https://api.deepseek.com', NULL, 'deepseek-chat', 0.70, 4096, 1);
