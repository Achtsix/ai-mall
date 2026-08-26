# 安全漏洞清单

## 严重漏洞（Critical）

### CVE-001: 管理员授权检查缺失
- **严重性**: 🔴 严重
- **文件**: `AdminController.java`
- **CVSS评分**: 9.1 (Critical)
- **影响**: 任何认证用户可能访问管理功能（如果拦截器路径配置错误）
- **根本原因**: JwtInterceptor 验证 `/api/admin/**` 路径的管理员角色，但在服务层没有二次验证
- **修复优先级**: P0 - 立即修复
- **修复工作量**: 中等

**漏洞详情**:
拦截器在请求到达控制器之前检查管理员角色（JwtInterceptor.java 第 45-52 行），但如果拦截器路径模式配置错误或被绕过，则没有防御纵深。

**修复建议**:
```java
// 在 AdminService 中添加权限检查
@PreAuthorize("hasRole('ADMIN')")
public void sensitiveOperation() {
    // 业务逻辑
}
```

### CVE-002: 文件上传漏洞
- **严重性**: 🔴 严重
- **文件**: `UploadController.java` (第 28-40 行)
- **CVSS评分**: 8.6 (High)
- **影响**: 
  - 上传恶意文件（webshell、病毒）
  - 路径遍历攻击
  - 存储耗尽（DoS）
- **修复优先级**: P0 - 立即修复
- **修复工作量**: 中等

**漏洞详情**:
- 无文件类型白名单验证
- 扩展名从用户控制的文件名中提取，未清理
- 无恶意文件内容检测
- 控制器中无文件大小验证（仅 Spring 的 multipart 配置）
- 如果发生文件名操作，可能存在目录遍历

**修复建议**:
```java
private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif");
private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

public Result<String> upload(MultipartFile file) {
    // 验证文件类型
    String ext = getExtension(file.getOriginalFilename());
    if (!ALLOWED_EXTENSIONS.contains(ext.toLowerCase())) {
        throw new BusinessException("不支持的文件类型");
    }
    
    // 验证文件大小
    if (file.getSize() > MAX_FILE_SIZE) {
        throw new BusinessException("文件过大");
    }
    
    // 生成安全文件名（不使用用户输入）
    String safeFilename = UUID.randomUUID().toString() + "." + ext;
    // ...
}
```

## 高严重性漏洞（High）

### VUL-003: API 密钥日志泄露风险
- **严重性**: 🟠 高
- **文件**: `DeepSeekClient.java` (第 118 行)
- **CVSS评分**: 7.5 (High)
- **影响**: API 密钥可能在错误日志中泄露
- **修复优先级**: P0 - 立即修复
- **修复工作量**: 小

**问题**: 错误日志包含完整响应体，可能包含来自上游的 API 密钥回显

**修复建议**: 在日志记录前编辑敏感字段

### VUL-004: 缺少 CSRF 保护
- **严重性**: 🟠 高
- **CVSS评分**: 6.8 (Medium)
- **影响**: 跨站请求伪造攻击
- **修复优先级**: P1 - 尽快修复
- **修复工作量**: 中等

**问题**: 状态变更操作无 CSRF 令牌验证

**修复建议**: 
- 为所有 POST/PUT/DELETE 请求实现 CSRF 令牌
- 使用 Spring Security 的 CSRF 保护

### VUL-005: 输入验证缺失
- **严重性**: 🟠 高
- **文件**: `UploadController.java`
- **影响**: 上传扩展名未验证
- **修复优先级**: P0 - 立即修复
- **修复工作量**: 小

## 中等严重性漏洞（Medium）

### VUL-006: 限流器内存泄漏
- **严重性**: 🟡 中
- **文件**: `AiRateLimiter.java`
- **CVSS评分**: 5.3 (Medium)
- **影响**: 内存泄漏风险
- **修复优先级**: P1 - 计划修复
- **修复工作量**: 中等

**问题**: ConcurrentHashMap 从不删除旧用户条目

**修复建议**: 实现定期清理或使用带 TTL 的缓存

### VUL-007: JWT 密钥配置
- **严重性**: 🟡 中
- **CVSS评分**: 5.0 (Medium)
- **影响**: 必须通过环境变量设置 JWT 密钥
- **修复优先级**: P2 - 文档说明
- **修复工作量**: 小

**当前状态**: 正确（从环境变量读取）  
**建议**: 在文档中强调密钥强度要求（至少 256 位）

### VUL-008: 弱密码策略
- **严重性**: 🟡 中
- **CVSS评分**: 4.5 (Medium)
- **影响**: 无复杂性要求
- **修复优先级**: P1 - 计划修复
- **修复工作量**: 小

**修复建议**:
```java
public void validatePassword(String password) {
    if (password.length() < 8) {
        throw new BusinessException("密码长度至少8位");
    }
    if (!password.matches(".*[A-Z].*")) {
        throw new BusinessException("密码必须包含大写字母");
    }
    if (!password.matches(".*[0-9].*")) {
        throw new BusinessException("密码必须包含数字");
    }
}
```

### VUL-009: 登录端点无暴力破解保护
- **严重性**: 🟡 中
- **CVSS评分**: 5.5 (Medium)
- **影响**: 暴力破解攻击
- **修复优先级**: P1 - 尽快修复
- **修复工作量**: 中等

**修复建议**: 实现登录尝试限流（每 IP 每小时最多 5 次失败）

## 低严重性问题（Low）

### VUL-010: CORS 配置过于宽松
- **严重性**: 🟢 低
- **文件**: `WebMvcConfig.java`
- **CVSS评分**: 3.5 (Low)
- **影响**: allowedOriginPatterns: "*"
- **修复优先级**: P2 - 生产前修复
- **修复工作量**: 小

**修复建议**: 限制为特定域名

### VUL-011: 详细错误消息
- **严重性**: 🟢 低
- **CVSS评分**: 3.0 (Low)
- **影响**: 可能泄露实现细节
- **修复优先级**: P3 - 优化
- **修复工作量**: 中等

## 安全加固建议

### 立即实施（P0）
1. ✅ 添加文件类型白名单到 UploadController
2. ✅ 实现文件内容验证
3. ✅ 在服务层添加二次管理员授权检查
4. ✅ 从错误日志中编辑敏感数据

### 高优先级（P1）
5. ✅ 实现登录尝试限流
6. ✅ 添加 CSRF 令牌验证
7. ✅ 实现限流器内存清理
8. ✅ 强制密码复杂性要求

### 中等优先级（P2）
9. ✅ 限制 CORS 到特定源
10. ✅ 添加请求/响应日志（带 PII 编辑）
11. ✅ 实现 API 密钥轮换机制
12. ✅ 添加安全头（CSP、X-Frame-Options 等）

## 统计

- **严重漏洞**: 2
- **高严重性**: 3
- **中等严重性**: 4
- **低严重性**: 2
- **总计**: 11 个安全问题

## 积极方面

✅ SQL 注入防护完善（所有 mapper 使用参数化查询）  
✅ 用户资源授权正确（Cart、Order、Address 验证所有权）  
✅ 密码哈希正确（BCrypt 加盐）  
✅ JWT 验证包括过期和签名检查  
✅ 限流算法线程安全
