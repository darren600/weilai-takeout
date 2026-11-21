# 味来外卖（WeiLai Takeout）

<p align="center">
  <img src="https://img.shields.io/badge/SpringBoot-3.2.5-brightgreen" alt="SpringBoot">
  <img src="https://img.shields.io/badge/Vue-3-brightgreen" alt="Vue">
  <img src="https://img.shields.io/badge/MySQL-8-brightgreen" alt="MySQL">
  <img src="https://img.shields.io/badge/Redis-brightgreen" alt="Redis">
  <img src="https://img.shields.io/badge/License-MIT-blue" alt="License">
</p>

一个基于 SpringBoot 3 + Vue 3 的前后端分离外卖点餐系统，包含用户端 H5 与管理端后台。项目由苍穹外卖脚手架二次开发而来，完成了界面重设计、移动端适配、数据重建与多项缺陷修复。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | Spring Boot 3.2.5、JDK 17、MyBatis-Plus、MySQL 8、Redis、WebSocket、JWT |
| 前端 | Vue 3、Vite、TypeScript、Element Plus、Axios、Pinia |
| 构建 | Maven（多模块）、npm |

## 项目结构

```
WeiLai/
├── docs/                          # 产品需求、架构设计、数据库 DDL、验收报告
├── sky-take-out/                  # 后端 Maven 多模块项目
│   ├── sky-common/                # 公共模块（常量、异常、工具类、属性配置）
│   ├── sky-pojo/                  # 实体类模块（DTO、VO、Entity）
│   └── sky-server/                # 服务模块（Controller、Service、Mapper）
└── frontend/                      # 前端项目（用户端 + 管理端）
    └── src/
        ├── api/                   # API 接口（user/admin）
        ├── views/                 # 页面组件（user/admin）
        ├── router/                # 路由配置
        └── stores/                # Pinia 状态管理
```

## 核心功能

### 用户端（H5）
- 📱 **短信验证码登录** - Redis 存储验证码，TTL 300s，一次性消费
- 🍔 **菜品/套餐浏览** - 分类查看、规格选择
- 🛒 **购物车** - 添加、删除、数量调整
- 📦 **订单管理** - 下单、订单查看、取消、催单
- 📍 **地址管理** - 新增、编辑、设为默认

### 管理端（Admin）
- 👤 **员工管理** - 登录、增删改查、启用/禁用
- 📂 **分类管理** - 菜品/套餐分类
- 🍳 **菜品管理** - 增删改查、起售/停售、规格配置
- 🎁 **套餐管理** - 增删改查、起售/停售
- 📊 **订单处理** - 接单、拒单、派送、完成
- 📈 **数据统计** - 营业额、订单量、用户分析、Top10 商品

### 实时通信
- 🔔 **WebSocket 推送** - 新订单提醒、催单通知

## 环境准备

### 1. 开发环境

| 软件 | 版本要求 |
|------|----------|
| JDK | 17+ |
| MySQL | 8.0+ |
| Redis | 6.0+ |
| Node.js | 18+ |
| Maven | 3.8+ |

### 2. 数据库初始化

```bash
# 创建数据库
CREATE DATABASE sky_take_out CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 导入 SQL 脚本
mysql -u root -p sky_take_out < docs/sql/weilai_take_out_ddl.sql
```

## 配置说明

> ⚠️ 后端真实配置文件 `application-dev.yml` **不在仓库中**（避免密钥泄露）。

### 配置文件模板

```bash
cd sky-take-out/sky-server/src/main/resources
cp application-dev.yml.example application-dev.yml
```

### 关键配置项

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sky_take_out
    username: root
    password: your_password  # 修改为你的数据库密码

  data:
    redis:
      host: localhost
      password: your_redis_password  # 修改为你的 Redis 密码

sky:
  alioss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    accessKeyId: your_access_key
    accessKeySecret: your_secret
    bucketName: your_bucket

  wechat:
    appid: your_appid
    secret: your_secret
    mchid: your_mchid
    # ... 其他微信支付配置
```

## 启动项目

### 后端启动

```bash
# 进入后端目录
cd sky-take-out

# 编译打包
mvn -pl sky-server -am package -DskipTests

# 启动服务
java -jar sky-server/target/sky-server-1.0-SNAPSHOT.jar --spring.profiles.active=dev
```

后端默认端口：**8080**

### 前端启动

```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端默认端口：**5173**  
管理端访问地址：`http://localhost:5173/admin`  
用户端访问地址：`http://localhost:5173`

## 接口文档

项目集成了 Swagger API 文档，启动后端服务后可访问：

- 管理端 API：http://localhost:8080/swagger-ui/admin.html
- 用户端 API：http://localhost:8080/swagger-ui/user.html

## 测试账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | 123456 |

> 📝 这是项目内置演示账号，仅用于开发测试，非真实生产环境凭据。

## 项目特性

- **前后端分离** - 清晰的架构分离，便于独立开发与部署
- **JWT 认证** - 管理员与用户两套独立的 Token 机制
- **Redis 缓存** - 菜品、套餐、短信验证码缓存
- **定时任务** - 订单超时自动取消、每日订单状态更新
- **WebSocket** - 实时推送新订单与催单提醒

## 注意事项

1. 本项目为学习与实践用途的 Demo
2. 支付功能需要商户资质，未接入真实支付渠道
3. 生产环境部署需自行配置 HTTPS、域名等
4. 请勿将包含真实密钥的配置文件提交到仓库

## 许可证

本项目基于 MIT 许可证开源，详见 [LICENSE](LICENSE) 文件。