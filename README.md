# astra-framework

基于 Spring Boot 4 的 Java Web 框架

## 功能模块

### 权限系统

| 功能 | 说明 | 核心类 |
|------|------|--------|
| Session 认证 | 基于 Cookie 的浏览器会话认证 | `SessionAuthFilter` |
| Token 认证 | Bearer Token / API Key 认证 | `TokenAuthResolver` |
| 权限注解 | `@RequiresPermission` 支持 AND/OR 语义 | `RequiresPermission` |
| 权限拦截 | Controller 层自动校验权限码 | `UserPermissionInterceptor` |
| RBAC 模型 | 用户→角色→权限，5 表 JOIN | `UserPermissionCheckService` |
| 菜单权限绑定 | 后台菜单树按权限码过滤 | `DashboardMenuServiceImpl` |
| 线程级上下文 | 当前请求用户信息 | `UserContextHolder` + `CurrentUserService` |
| 白名单放行 | 无需认证的路径配置 | `SessionAuthFilter.WHITE_LIST` |

### 请求限流

| 功能 | 说明 | 核心类 |
|------|------|--------|
| 接口限流 | 方法级限流注解，支持时间窗口/最大次数 | `@RateLimit` |
| 限流切面 | AOP 实现，基于缓存计数 | `RateLimitAspect` |
| 限流异常处理 | 统一返回 429 状态码 | `RateLimitException` + `GlobalExceptionHandler` |

### 缓存

| 功能 | 说明 | 核心类 |
|------|------|--------|
| 缓存抽象接口 | 统一的缓存操作 API | `CacheService` |
| 内存缓存 | Caffeine 本地缓存实现 | `MemoryCacheServiceImpl` |
| Redis 缓存 | Redis 远程缓存实现 | `RedisCacheServiceImpl` |
| 自动配置 | 自动检测 Redis 可用性，fallback 内存模式 | `CacheAutoConfiguration` |

### 安全

| 功能 | 说明 | 核心类 |
|------|------|--------|
| Session 劫持防护 | User-Agent 绑定校验 | `UserAgentSessionFilter` |
| 用户黑名单 | 基于缓存的用户封禁管理 | `UserBlacklistService` |
| 密码加密 | BCrypt 哈希存储 | `PasswordUtil` |
| 全局异常处理 | 统一异常拦截与响应 | `GlobalExceptionHandler` |

### 用户模块

| 功能 | 说明 | 核心类 |
|------|------|--------|
| 登录 | 用户名+密码+验证码校验 | `LoginController` |
| 登出 | Session 失效+清理 | `LoginController` |
| 注册 | 用户名/手机号/邮箱+验证码+默认角色 | `RegisterController` |
| 验证码 | 图形验证码生成+Redis 缓存校验 | `CaptchaController` + `CaptchaUtil` |
| 密码找回 | 邮箱验证码→验证→重置密码，含安全策略 | `ForgotPasswordController` + `ForgotPasswordService` |
| API Key 管理 | 创建/列表/启用/禁用/删除 API Key | `ApiKeyService` |

### 消息通知

| 功能 | 说明 | 核心类 |
|------|------|--------|
| 系统通知 | 发送/分页查询/已读/未读/删除 | `NotificationService` + `NotificationServiceImpl` |
| 通知 API | REST 接口获取通知列表 | `SystematicNotificationController` |
| 邮件发送 | 简单文本/HTML/附件/模板邮件 | `MailService` + `MailServiceImpl` |
| 邮件配置 | SMTP 配置从数据库读取 | `SiteConfig` → `MailServiceImpl` |

### 初始化安装

| 功能 | 说明 | 核心类 |
|------|------|--------|
| 首次运行检测 | 检测配置文件，自动进入初始化模式 | `StartServer` + `ConfigInfo` |
| 安装向导 | JSP 页面引导配置 | `InitController` |
| 自动建库 | 自动创建 MySQL 数据库 | `DatabaseCreator` |
| 数据库迁移 | Flyway 基线+版本管理 | `FlywayConfig` |
| 配置生成 | 自动生成 `application.yml` | `InitController.generateApplicationYml` |
| SQL 初始化 | 执行 `init.sql` 创建表结构+种子数据 | `InitController.initializeDatabase` |

### 基础设施

| 功能 | 说明 | 核心类 |
|------|------|--------|
| 统一响应结构 | 通用 JSON 响应格式 | `ApiResponse<T>` |
| 分页 DTO | 通用分页结果封装 | `PageResult<T>` |
| MyBatis 映射 | 完整 MyBatis XML 映射配置 | `mybatis-config.xml` |
| 日志配置 | Logback 滚动日志+异步输出 | `logback-spring.xml` |
| 跨域支持 | Nginx 代理配置 + Spring CORS | `WebConfig` |
| REST 客户端 | RestTemplate Bean | `WebConfig` |

### 工具类

| 功能 | 说明 | 核心类 |
|------|------|--------|
| 大模型调用 | OpenAI 兼容 API 调用，含重试机制 | `LargeLanguageModelsUtil` |
| 图片压缩 | Thumbnailator 图片质量压缩 | `ImageCompressorUtil` |
| 随机数生成 | UUID+时间戳+节点 ID 混合 | `RandomUtil` |
| 验证码工具 | 验证码生成+校验 | `CaptchaUtil` |
| URL 处理 | 完整 URL 拼接与解析 | `UrlUtil` |
| 域名获取 | 动态获取全域名（支持 X-Forwarded 代理头） | `DomainUtil` |
| Bot 检测 | 搜索引擎爬虫识别 | `BotUtil` |
| API Key 生成 | SHA-256 哈希 API Key | `ApiKeyUtil` |

### 后台菜单

| 功能 | 说明 | 核心类 |
|------|------|--------|
| 菜单管理 | 树形结构后台菜单 | `DashboardMenu` domain |
| 菜单 API | 按权限过滤的菜单查询 | `DashboardController` |
| 菜单服务 | 构建菜单树+权限过滤 | `DashboardMenuServiceImpl` |

### 附件系统

| 功能 | 说明 | 核心类 |
|------|------|--------|
| 文件上传 | 单文件上传+分片上传 | `UploadController` + `UploadService` |
| 文件访问 | 预览/下载 | `FileAccessController` + `FileAccessService` |
| 图片压缩 | 异步压缩 | `ImageCompressionService` |
| 附件管理 | CRUD + 软删除 | `AttachmentManagementController` + `AttachmentManagementService` |
| 外部资源代理 | 代理下载外部资源 | `UploadService.downloadAndUploadExternalResource` |

### 前端导航

| 功能 | 说明 | 核心类 |
|------|------|--------|
| 导航菜单管理 | CRUD + 排序 | `MenusController` + `MenusService` |

## 数据库表

| 表名 | 说明 | 所属模块 |
|------|------|----------|
| `user_information` | 用户账户表 | 用户模块 |
| `role` | 角色定义表 | 权限系统 |
| `permission` | 权限码定义表 | 权限系统 |
| `user_role` | 用户-角色关联表 | 权限系统 |
| `role_permission` | 角色-权限关联表 | 权限系统 |
| `api_key` | API Key 存储表 | 用户模块 |
| `site_config` | 站点配置表（含 SMTP/注册开关等） | 基础设施 |
| `systematic_notification` | 系统通知表 | 消息通知 |
| `dashboard_menu` | 后台菜单表 | 权限系统 |
| `menus` | 前端导航菜单表 | 前端导航 |
| `attachment` | 附件记录表 | 附件系统 |

## 响应格式

所有接口统一返回 `ApiResponse<T>`：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

## 开发规范

- 控制器只做参数绑定 + 委托 Service，不包含业务逻辑
- 所有 POST/PUT 请求必须使用 DTO
- 所有端点方法加单行中文注释
- 返回统一使用 `ApiResponse<T>`

