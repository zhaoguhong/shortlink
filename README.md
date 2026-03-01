# shortlink

[![Java](https://img.shields.io/badge/Java-17-blue)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-6DB33F)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-Apache--2.0-orange)](./LICENSE)

短链（Short URL）是把一个很长的原始链接压缩成一个更短、可读性更好的链接。  
用户访问短链时，服务端会把它重定向到原始地址，同时可以记录访问数据（如 PV、UV）。

常见使用场景包括：

- 分享链接时减少长度，提升美观和可传播性
- 在短信、海报、社媒等场景节省字符空间
- 在不暴露原始链接的情况下统一管理跳转目标
- 统计访问效果（例如访问量、访客数、最近访问时间）

这个项目是一个基于 **Spring Boot 3 + MyBatis + MySQL + Redis** 的短链系统示例，采用多模块 Maven 结构，包含短链创建、重定向和访问统计能力。

## 目录

- [核心能力与亮点](#核心能力与亮点)
- [架构概览](#架构概览)
- [模块说明](#模块说明)
- [快速开始](#快速开始)
- [API 概览](#api-概览)
- [设计说明](#设计说明)
- [后续规划（Roadmap）](#后续规划roadmap)
- [License](#license)

## 核心能力与亮点

- 分层清晰：访问服务与管理服务分离，便于独立扩展
- 管理端 CRUD：创建、更新、删除、查询短链
- 短链重定向：`GET /{code}` 返回 `302 Found`
- 策略化短码生成：支持 Redis Base62 / MurmurHash，可按配置切换，并支持冲突重试
- 访问统计：记录 PV、UV（基于 `IP + User-Agent`）和最近访问时间
- 缓存友好：正常缓存 + 空值缓存，降低数据库压力
- 易于上手：提供完整 `schema.sql`、配置说明和启动命令

## 架构概览

```text
Client
  | 1) POST /api/links
  v
shortlink-admin (8081)
  |-- 写入 MySQL (short_link)
  |-- 生成短码 (strategy)
  `-- 维护统计元信息

Client
  | 2) GET /{code}
  v
shortlink-server (8080)
  |-- 优先读 Redis 缓存
  |-- 未命中回源 MySQL
  `-- 302 重定向到 originalUrl
```

## 模块说明

- `shortlink-server`：短链访问服务（默认端口 `8080`）
- `shortlink-admin`：短链管理 API（默认端口 `8081`）
- `shortlink-common`：公共模块（统一返回体、异常处理等）
- `schema.sql`：数据库初始化脚本

## 快速开始

### 1. 初始化数据库

在项目根目录执行：

```bash
mysql -u root -p < schema.sql
```

### 2. 配置应用

按本地环境修改：

- `shortlink-server/src/main/resources/application.yml`
- `shortlink-admin/src/main/resources/application.yml`

<details>
<summary>查看默认配置（示例）</summary>

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
</details>

> 提示：以上账号密码仅为本地开发示例，请按你的环境修改，避免直接用于生产环境。

### 3. 构建项目

```bash
mvn clean package
```

### 4. 启动服务

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

常用接口：

- `POST /api/links`：创建短链（管理服务，`8081`）
- `GET /{code}`：访问短链并 `302` 重定向（访问服务，`8080`）

> 其他管理接口：`/api/links/{id}/update`、`/api/links/{id}/delete`、`/api/links/{id}`、`/api/links`

创建短链请求示例：

```bash
curl -X POST "http://localhost:8081/api/links" \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://www.example.com/path?a=1",
    "status": 1,
    "expireTime": "2026-12-31T23:59:59"
  }'
```

创建成功后，访问短链示例：

```http
GET http://localhost:8080/aB3dE9
```

短码不存在、已过期或被禁用时返回 `404`。  
管理接口返回统一结构，示例如下：

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

## 设计说明

- 重定向链路优先读 Redis，未命中时回源 MySQL 并写回缓存
- 通过空值缓存降低不存在短码请求造成的数据库压力
- UV 使用按天 Set 统计，键过期时间默认 7 天
- 目前统计数据主要落在 Redis，后续可通过任务回刷数据库

## 后续规划（Roadmap）

- [x] 自动生成短链码（支持 Redis Base62 / MurmurHash 策略化配置）
- [x] 短链缓存与空值缓存
- [ ] 按天统计落表（例如 `short_link_stats_daily`）
- [ ] Redis 增量异步回刷 MySQL
- [ ] 后台登录与权限体系
- [ ] 管理端 Web UI（Vue/React）

## License

Apache License 2.0（`Apache-2.0`），详见 `LICENSE`。

