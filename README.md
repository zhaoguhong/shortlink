# shortlink

一个基于 **Spring Boot 3 + MyBatis + MySQL + Redis** 的短链接服务示例项目，采用多模块 Maven 结构，包含：

- 短链跳转服务（`shortlink-server`）
- 短链管理后台 API（`shortlink-admin`）
- 公共模块（`shortlink-common`）

适合用于学习和实践：短链映射、重定向、访问统计（PV/UV）、多模块拆分与基础工程化。

## 技术栈

- JDK 17
- Spring Boot 3.3.4
- MyBatis
- MySQL
- Redis
- Maven

## 功能概览

- 短链访问：`GET /{code}` 根据短码重定向到原始 URL
- 访问统计：记录短链 PV / UV / 最近访问时间（当前落在 Redis）
- 后台管理：提供短链 CRUD REST API
- 公共能力：统一返回体、业务异常、全局异常处理

## 项目结构

```text
shortlink
├── shortlink-common   # 公共模块（响应体、异常等）
├── shortlink-server   # 短链访问服务（端口 8080）
├── shortlink-admin    # 短链管理后台（端口 8081）
└── schema.sql         # MySQL 建表脚本
```

统一根包名：`com.zhaoguhong.shortlink`

## 快速开始

### 1) 准备环境

- JDK 17+
- Maven 3.8+
- MySQL 8.x
- Redis 6.x+

### 2) 初始化数据库

先创建数据库和表：

```bash
mysql -u root -p < schema.sql
```

### 3) 配置服务

根据本地环境修改以下配置文件：

- `shortlink-server/src/main/resources/application.yml`
- `shortlink-admin/src/main/resources/application.yml`

默认示例配置：

- MySQL：`jdbc:mysql://localhost:3306/shortlink`
- 用户名：`root`
- 密码：`root`
- Redis：`localhost:6379`
- Redis DB：`shortlink-server` 使用 `0`，`shortlink-admin` 使用 `1`

### 4) 构建项目

在项目根目录执行：

```bash
mvn clean package
```

### 5) 启动服务

启动短链访问服务（8080）：

```bash
cd shortlink-server
mvn spring-boot:run
```

启动管理后台（8081）：

```bash
cd shortlink-admin
mvn spring-boot:run
```

## API 说明

### shortlink-server

- `GET /{code}`：按短链码重定向到原始链接（302）

示例：

```http
GET http://localhost:8080/abc123
```

### shortlink-admin

- `POST /api/links`：创建短链
- `POST /api/links/{id}/update`：更新短链
- `POST /api/links/{id}/delete`：删除短链
- `GET /api/links/{id}`：查询详情
- `GET /api/links`：查询列表

创建示例：

```bash
curl -X POST "http://localhost:8081/api/links" \
  -H "Content-Type: application/json" \
  -d '{
    "originalUrl": "https://www.example.com",
    "status": 1
  }'
```

## 数据模型

建表脚本位于：`schema.sql`

- `short_link`：短链主表，存储 `code` 与 `original_url` 映射
- `short_link_stats`：短链统计表，聚合 `total_pv`、`total_uv`、`recent_access_time`

## 开发说明

- 当前统计逻辑以 Redis 计数为主，后续可通过定时任务回刷 MySQL
- 短链码已改为服务端 Base62 自动生成（含冲突重试）
- 目前为 API 工程，未包含前端管理界面

## Roadmap

- [x] 自动生成短链码（Base62）
- [ ] 按天统计访问数据（例如 `short_link_stats_daily`）
- [ ] Redis 增量异步回刷 MySQL
- [ ] 后台登录与权限体系
- [ ] 管理端 Web UI（Vue/React）

## License

Apache License 2.0（`Apache-2.0`），详见根目录 `LICENSE` 文件。

