# 味来外卖（WeiLai Takeout）

一个基于 SpringBoot 3 + Vue 3 的前后端分离外卖点餐系统，包含用户端 H5 与管理端后台。项目由苍穹外卖脚手架二次开发而来，完成了界面重设计、移动端适配、数据重建与多项缺陷修复。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Spring Boot 3.2.5、JDK 17、MyBatis-Plus、MySQL 8、Redis、WebSocket、JWT |
| 前端 | Vue 3、Vite、TypeScript、Element Plus、Axios、Pinia |
| 构建 | Maven（多模块）、npm |

## 目录结构

```
WeiLai/
├── docs/                     产品需求、架构设计、数据库 DDL、验收报告
├── sky-take-out/             后端（Maven 多模块：common / pojo / server）
└── frontend/                 前端（用户端 + 管理端）
```

## 核心功能

- 短信验证码登录（Redis 存储验证码，TTL 300s，一次性消费）
- 用户端：分类浏览、菜品/套餐选购、规格选择、购物车、下单、订单查看、地址管理
- 管理端：员工登录、分类/菜品/套餐管理、订单处理、数据统计报表
- WebSocket 实时推送新订单与催单提醒

## 本地启动

### 1. 环境准备

- JDK 17、MySQL 8、Redis、Node.js 18+
- 创建数据库 `sky_take_out` 并执行 `docs/03-数据库/weilai_take_out_ddl.sql`

### 2. 配置（重要）

后端真实配置文件 `application-dev.yml` **不在仓库中**（避免密钥泄露）。请按以下步骤生成：

```bash
cd sky-take-out/sky-server/src/main/resources
cp application-dev.yml.example application-dev.yml
```

然后编辑 `application-dev.yml`，填入你自己的数据库密码、Redis 密码、阿里云 OSS 密钥和微信小程序密钥。

### 3. 启动服务

```bash
# 后端（在 sky-take-out 目录）
mvn -Dmaven.repo.local=<你的本地仓库路径> -pl sky-server -am package -DskipTests
java -jar sky-server/target/sky-server-1.0-SNAPSHOT.jar --spring.profiles.active=dev

# 前端（在 frontend 目录）
npm install
npm run dev
```

前端默认 `http://localhost:5173`，后端 `http://localhost:8080`。

## 测试账号

- 管理端：`admin` / `123456`（这是项目内置演示账号，非真实凭据）

## 说明

本项目为学习与实践用途的 Demo，支付等功能需要商户资质，未接入真实支付渠道。
