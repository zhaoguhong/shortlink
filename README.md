# shortlink

基于 **Spring Boot 3 + MyBatis + MySQL + Redis** 的短链接服务，采用多模块 Maven 结构，包含短链创建、重定向和访问统计能力。

## 模块说明

- `shortlink-server`：短链访问服务（默认端口 `8080`）
- `shortlink-admin`：短链管理 API（默认端口 `8081`）
- `shortlink-common`：公共模块（统一返回体、异常处理等）
- `schema.sql`：数据库初始化脚本

## 技术栈

- JDK 17
- Spring Boot 3.3.4
- MyBatis
- MySQL 8.x
- Redis 6.x+
- Maven 3.8+

## 核心能力

- 短链重定向：`GET /{code}` 返回 `302 Found`
- 管理端 CRUD：创建、更新、删除、查询短链
- 短码生成：支持可配置策略（Redis 递增 Base62 / MySQL ID Base62 / MurmurHash，含冲突重试）
- 访问统计：记录 PV、UV（基于 `IP + User-Agent`）和最近访问时间
- 缓存策略：
  - 正常短链缓存（可配置 TTL）
  - 空值缓存（防缓存穿透）

## 快速开始

### 1. 环境准备

- JDK 17+
- Maven 3.8+
- MySQL 8.x
- Redis 6.x+

### 2. 初始化数据库

在项目根目录执行：

```bash
mysql -u root -p < schema.sql
```

### 3. 配置应用

按本地环境修改：

- `shortlink-server/src/main/resources/application.yml`
- `shortlink-admin/src/main/resources/application.yml`

当前默认配置（代码仓库内）：

- MySQL URL：`jdbc:mysql://localhost:3306/shortlink?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai`
- MySQL 用户名：`root`
- MySQL 密码：`123456`
- Redis：`localhost:6379`

`shortlink-server` 额外缓存参数：

- `shortlink.cache.code-ttl-seconds`：正常短链缓存 TTL（秒，`<=0` 表示禁用）
- `shortlink.cache.null-ttl-seconds`：空值缓存 TTL（秒，`<=0` 表示禁用）

`shortlink-admin` 额外短码生成参数：

- `shortlink.codegen.strategy`：短码生成策略，支持 `redis-base62`、`murmur-hash-base62`
- `shortlink.codegen.murmur.length`：MurmurHash 短码长度（`murmur-hash-base62` 生效）

### 4. 构建项目

```bash
mvn clean package
```

### 5. 启动服务

方式一（分别启动）：

```bash
cd shortlink-server && mvn spring-boot:run
cd shortlink-admin && mvn spring-boot:run
```

方式二（根目录按模块启动）：

```bash
mvn -pl shortlink-server spring-boot:run
mvn -pl shortlink-admin spring-boot:run
```

## API 概览

### 1) 访问服务（`shortlink-server`）

- `GET /{code}`：重定向到原始链接（`302`）

示例：

```http
GET http://localhost:8080/abc123
```

当短码不存在、已过期或被禁用时返回 `404`。

### 2) 管理服务（`shortlink-admin`）

- `POST /api/links`：创建短链
- `POST /api/links/{id}/update`：更新短链
- `POST /api/links/{id}/delete`：删除短链
- `GET /api/links/{id}`：查询详情
- `GET /api/links`：查询列表

创建短链示例：

```bash
curl -X POST "http://localhost:8081/api/links" \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://www.example.com/path?a=1",
    "status": 1,
    "expireTime": "2026-12-31T23:59:59"
  }'
```

返回结构（统一包装）：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "code": "aB3dE9",
    "originalUrl": "https://www.example.com/path?a=1",
    "status": 1,
    "expireTime": "2026-12-31T23:59:59"
  }
}
```

## 数据模型

建表脚本：`schema.sql`

- `short_link`：短链主表，维护 `code` 和 `original_url` 映射
- `short_link_stats`：统计表（`total_pv`、`total_uv`、`recent_access_time`）

## 设计说明

- 重定向链路优先读 Redis，未命中时回源 MySQL 并写回缓存
- 通过空值缓存降低不存在短码请求造成的数据库压力
- UV 使用按天 Set 统计，键过期时间默认 7 天
- 目前统计数据主要落在 Redis，后续可通过任务回刷数据库

## Roadmap

- [x] 自动生成短链码（支持 Redis Base62 / MurmurHash 策略化配置）
- [x] 短链缓存与空值缓存
- [ ] 按天统计落表（例如 `short_link_stats_daily`）
- [ ] Redis 增量异步回刷 MySQL
- [ ] 后台登录与权限体系
- [ ] 管理端 Web UI（Vue/React）

## License

Apache License 2.0（`Apache-2.0`），详见 `LICENSE`。

