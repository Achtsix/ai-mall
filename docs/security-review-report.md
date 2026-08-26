# 安全审查报告

## 审查概述

本报告对 AI Mall 项目进行了全面的安全审计，涵盖 6 个关键领域：
1. JWT 认证与授权
2. API 密钥管理
3. SQL 注入保护
4. 权限控制
5. 限流机制
6. 文件上传安全

## 执行摘要

**整体安全等级**: ⚠️ 中等风险

发现了 **2 个严重漏洞**需要立即修复，但核心安全实践（SQL 注入防护、密码哈希、JWT 验证）实施良好。

### 关键发现

✅ **安全的方面**:
- SQL 注入防护：所有 mapper 正确使用参数化查询
- 用户资源授权：Cart、Address、Order 服务正确验证所有权
- 密码哈希：BCrypt 正确实施加盐
- JWT 验证：令牌验证包括过期和签名检查
- 限流算法：每用户滑动窗口实现线程安全

❌ **需要立即关注的问题**:
1. 管理员授权检查缺失（严重）
2. 文件上传安全漏洞（严重）
3. API 密钥可能泄露到错误日志（高）

## 详细审查结果

### 1. JWT 认证与授权 ✓ 基本安全

#### 审查文件
- `JwtUtil.java`
- `UserContext.java`
- `JwtInterceptor.java`
- `WebMvcConfig.java`

#### 安全实践
✅ 令牌生成、验证和过期正确实现  
✅ 签名验证使用配置的密钥 HMAC  
✅ UserContext 在请求完成后正确清理  
✅ 拦截器正确配置路径模式

#### 发现的问题
⚠️ JWT 密钥必须通过环境变量提供（无默认值）  
⚠️ 无密钥强度验证（应至少 256 位）

**建议**:
```java
// 启动时验证密钥强度
if (jwtSecret.length() < 32) {
    throw new IllegalStateException("JWT 密钥长度至少 256 位（32 字符）");
}
```

### 2. API 密钥管理 ✓ 安全但有小问题

#### 审查文件
- `DeepSeekClient.java`
- `application.yml`

#### 安全实践
✅ API 密钥仅从环境变量加载  
✅ 未发现硬编码凭证  
✅ 密钥验证防止占位符值（第 221-223 行）

#### 发现的问题
⚠️ 错误日志中潜在泄露（第 118 行）

**问题代码**:
```java
log.error("AI API 调用失败: {}", response.body());
```

响应体可能包含来自上游的 API 密钥回显。

**修复建议**:
```java
// 日志前编辑敏感字段
String safeBody = redactSensitiveFields(response.body());
log.error("AI API 调用失败: {}", safeBody);
```

### 3. SQL 注入保护 ✓ 完全保护

#### 审查文件
- 所有 27 个 mapper 文件（`backend/src/main/resources/mapper/*.xml`）

#### 安全实践
✅ 所有 mapper 使用 MyBatis 参数化查询 (`#{}`)  
✅ `ProductMapper.java` 中的动态 SQL 正确使用带参数绑定的 MyBatis `<if>` 标签  
✅ 未发现不安全的字符串插值 (`${}`)

**示例（安全）**:
```xml
<select id="search" resultType="Product">
    SELECT * FROM product WHERE 1=1
    <if test="keyword != null">
        AND name LIKE CONCAT('%', #{keyword}, '%')
    </if>
    <if test="minPrice != null">
        AND price >= #{minPrice}
    </if>
</select>
```

### 4. 授权与访问控制 ⚠️ 部分安全

#### 用户资源保护 ✓ 安全

**审查的服务**:
- `OrderService.java` - 所有操作验证 userId
- `CartService.java` - 强制 userId 匹配
- `AddressService.java` - 强制 userId 匹配
- `WalletService.java` - 检查所有权

**示例（安全）**:
```java
// OrderService.java
public Order detail(String orderNo) {
    Long userId = UserContext.getUserId();
    Order order = orderMapper.findByOrderNo(orderNo);
    if (!order.getUserId().equals(userId)) {
        throw new BusinessException("无权访问此订单");
    }
    return order;
}
```

#### 管理员保护 ⚠️ 仅依赖拦截器

**问题**:
- 拦截器检查 `/api/admin/**`、`/api/ai/tools`、`/api/ai/knowledge` 的管理员角色
- 服务层无二次验证
- 如果路径模式改变或拦截器被绕过则存在风险

**拦截器代码**（JwtInterceptor.java 第 45-52 行）:
```java
if (path.startsWith("/api/admin/") || 
    path.startsWith("/api/ai/tools") ||
    path.startsWith("/api/ai/knowledge")) {
    if (!"ADMIN".equals(user.getRole())) {
        throw new BusinessException("需要管理员权限");
    }
}
```

**修复建议**: 在服务层添加防御纵深
```java
@PreAuthorize("hasRole('ADMIN')")
public void adminOperation() {
    // 业务逻辑
}
```

### 5. 限流机制 ✓ 安全但需优化

#### 审查文件
- `AiRateLimiter.java`

#### 安全实践
✅ 线程安全的每用户滑动窗口实现  
✅ 并发访问正确同步  
✅ 限流逻辑正确（12 请求/分钟）

#### 发现的问题
⚠️ 内存泄漏风险：ConcurrentHashMap 从不删除旧用户条目

**问题代码**:
```java
private final ConcurrentHashMap<Long, Deque<Long>> userRequests = new ConcurrentHashMap<>();

public boolean allowRequest(Long userId) {
    // 添加到 map，但从不清理不活跃用户
}
```

**修复建议**:
```java
// 选项 1: 使用 Caffeine Cache 带 TTL
private final Cache<Long, Deque<Long>> userRequests = 
    Caffeine.newBuilder()
        .expireAfterAccess(Duration.ofHours(1))
        .build();

// 选项 2: 定期清理
@Scheduled(fixedRate = 3600000) // 每小时
public void cleanup() {
    long cutoff = System.currentTimeMillis() - 3600000;
    userRequests.entrySet().removeIf(entry -> 
        entry.getValue().peekLast() < cutoff
    );
}
```

### 6. 文件上传安全 ✗ 存在漏洞

#### 审查文件
- `UploadController.java` (第 28-40 行)

#### 发现的问题

❌ **无文件类型验证**
```java
// 当前代码（不安全）
String ext = fileName.substring(fileName.lastIndexOf(".") + 1);
String newName = UUID.randomUUID() + "." + ext;
```

任何扩展名都被接受：`.jsp`、`.exe`、`.sh`

❌ **无内容验证**  
未检测恶意文件内容或验证 MIME 类型

❌ **控制器中无大小限制**  
仅依赖 Spring 的 multipart 配置

❌ **路径遍历风险**  
如果文件名操作不当可能发生

❌ **直接提供上传文件**  
通过 `/uploads/products/...` 直接提供，无清理

**修复建议**:
```java
@PostMapping("/upload")
public Result<String> upload(@RequestParam("file") MultipartFile file) {
    // 1. 验证文件类型
    String originalName = file.getOriginalFilename();
    String ext = getExtension(originalName);
    
    Set<String> ALLOWED = Set.of("jpg", "jpeg", "png", "gif", "webp");
    if (!ALLOWED.contains(ext.toLowerCase())) {
        throw new BusinessException("只允许图片文件");
    }
    
    // 2. 验证 MIME 类型
    String contentType = file.getContentType();
    if (!contentType.startsWith("image/")) {
        throw new BusinessException("无效的文件类型");
    }
    
    // 3. 验证大小
    if (file.getSize() > 5 * 1024 * 1024) {
        throw new BusinessException("文件不能超过 5MB");
    }
    
    // 4. 生成安全文件名（不使用用户输入）
    String safeName = UUID.randomUUID().toString() + "." + ext;
    
    // 5. 验证图片内容（防止隐藏的 webshell）
    try {
        BufferedImage img = ImageIO.read(file.getInputStream());
        if (img == null) {
            throw new BusinessException("无效的图片文件");
        }
    } catch (IOException e) {
        throw new BusinessException("图片处理失败");
    }
    
    // 6. 保存到安全位置
    Path uploadPath = Paths.get(UPLOAD_DIR, safeName);
    Files.copy(file.getInputStream(), uploadPath);
    
    return Result.success("/uploads/products/" + safeName);
}
```

## 其他安全问题

### 弱密码策略
**文件**: `AuthService.java`  
**问题**: 无复杂性要求  
**建议**: 强制最小长度 8 位，包含大写、小写、数字

### 无暴力破解保护
**端点**: `/api/auth/login`  
**问题**: 无登录尝试限流  
**建议**: 每 IP 每小时限制 5 次失败尝试

### CORS 配置过于宽松
**文件**: `WebMvcConfig.java`  
**问题**: `allowedOriginPatterns("*")`  
**建议**: 限制为特定域名
```java
.allowedOriginPatterns(
    "http://localhost:5173",
    "https://yourdomain.com"
)
```

### 详细错误消息
**问题**: 某些错误消息可能泄露实现细节  
**建议**: 在生产环境使用通用错误消息

## 审查的文件清单

### 认证与授权
- ✅ `/backend/src/main/java/com/aimall/common/JwtUtil.java`
- ✅ `/backend/src/main/java/com/aimall/common/UserContext.java`
- ✅ `/backend/src/main/java/com/aimall/interceptor/JwtInterceptor.java`
- ✅ `/backend/src/main/java/com/aimall/config/WebMvcConfig.java`

### 控制器（授权检查）
- ✅ `/backend/src/main/java/com/aimall/controller/AdminController.java`
- ✅ `/backend/src/main/java/com/aimall/controller/OrderController.java`
- ✅ `/backend/src/main/java/com/aimall/controller/CartController.java`
- ✅ `/backend/src/main/java/com/aimall/controller/AddressController.java`
- ✅ `/backend/src/main/java/com/aimall/controller/UploadController.java`

### 服务（业务逻辑安全）
- ✅ `/backend/src/main/java/com/aimall/service/OrderService.java`
- ✅ `/backend/src/main/java/com/aimall/service/CartService.java`
- ✅ `/backend/src/main/java/com/aimall/service/AddressService.java`
- ✅ `/backend/src/main/java/com/aimall/service/UserService.java`

### 数据访问（SQL 注入）
- ✅ 所有 27 个 mapper 文件 (`/backend/src/main/java/com/aimall/mapper/*.xml`)

### AI 集成
- ✅ `/backend/src/main/java/com/aimall/ai/DeepSeekClient.java`
- ✅ `/backend/src/main/java/com/aimall/ai/AiRateLimiter.java`

### 配置
- ✅ `/backend/src/main/resources/application.yml`

## 修复优先级矩阵

| 优先级 | 漏洞 | 严重性 | 工作量 |
|-------|------|--------|--------|
| P0 | 文件上传类型验证 | 严重 | 中 |
| P0 | 文件内容验证 | 严重 | 中 |
| P0 | 管理员服务层授权 | 严重 | 中 |
| P0 | API 密钥日志编辑 | 高 | 小 |
| P1 | 登录尝试限流 | 中 | 中 |
| P1 | CSRF 令牌验证 | 高 | 中 |
| P1 | 限流器内存清理 | 中 | 中 |
| P1 | 密码复杂性要求 | 中 | 小 |
| P2 | 限制 CORS 源 | 低 | 小 |
| P2 | API 密钥轮换 | 中 | 大 |
| P2 | 安全头 | 低 | 小 |

## 总结

代码库在 SQL 注入防护和用户资源授权方面展现了良好的安全实践，但**文件上传验证**和**管理员操作的防御纵深**需要立即关注。

**推荐行动**:
1. 在生产部署前修复 2 个严重漏洞
2. 实施所有 P1 修复（估计 2-3 天工作）
3. 建立安全审查流程（代码审查清单）
4. 添加自动化安全测试（SAST/DAST）

**整体评估**: 项目有坚实的安全基础，但需要修复关键漏洞才能投入生产。
