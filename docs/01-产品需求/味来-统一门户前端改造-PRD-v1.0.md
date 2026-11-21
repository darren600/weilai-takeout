# 味来（Weilai）统一门户前端改造 PRD

| 项目 | 内容 |
|------|------|
| 版本 | v1.0 |
| 作者 | 司南（产品设计） |
| 日期 | 2026-11-17 |
| 项目来源 | 基于苍穹外卖后端（跟敲学习项目）改造，品牌名已替换为「味来」 |
| 文档目的 | 指导前端从零搭建 + 后端最小改造，产出可用于简历演示的完整项目 |
| 变更记录 | v1.0 首版：确定统一门户 + 角色分流方案 |

---

## 一、背景与目标

### 1.1 背景
现有项目为黑马「苍穹外卖」后端跟敲成果，仅含 Spring Boot 后端（sky-common / sky-pojo / sky-server 三模块），无前端。原始学习内容包括：双端 JWT 鉴权、Redis 菜品缓存、WebSocket 来单/催单提醒、阿里云 OSS 图片上传、ECharts 数据统计报表、Excel 导出。

### 1.2 要解决的问题
1. 项目只有后端，简历上无法呈现完整可演示的产品。
2. 原用户端依赖微信小程序，演示门槛高（需微信开发者工具 + AppID + 真机）。
3. 项目名与教学项目雷同，简历辨识度低。

### 1.3 目标（可衡量）
| 目标 | 衡量口径 | 目标值 |
|------|----------|--------|
| 形成可演示闭环 | 浏览器内可从登录走到下单完成 | 1 条完整链路 |
| 覆盖现有接口 | 后端已实现接口被前端调用的比例 | ≥ 80%（≥ 37/46） |
| 简历呈现 | 形成「统一认证 + 双端分流 + 数据可视化」三个可讲述亮点 | 3 个 |

### 1.4 约束
- 后端学习代码尽量不改，仅做**最小增量改造**（见第四章）。
- 技术选型面向实习面试主流栈，不引入偏门技术。

---

## 二、名词与术语

| 术语 | 定义 |
|------|------|
| 统一门户 | 单一 Web 登录入口，用户先选身份（管理员/用户）再登录 |
| 角色分流 | 登录成功后按 role 跳转不同前端子应用，携带各自的 JWT |
| 双 JWT | 管理端 token（header=`token`，claims=`empId`）与用户端 token（header=`authentication`，claims=`userId`）两套独立令牌 |
| Mock 用户登录 | 为网站用户端提供的测试登录方式，绕过微信 code（详见 4.2） |

---

## 三、需求范围

### 3.1 本期包含
- 统一登录门户（身份选择 + 双表单）
- 管理端前端：工作台、订单管理、菜品管理、套餐管理、分类管理、数据统计
- 用户端前端：点餐首页、菜品详情、购物车、确认订单、订单列表、订单详情
- 后端最小增量：用户端网站登录接口、CORS 跨域配置、图片兜底

### 3.2 本期不包含（明确不做）
- 真实微信支付 / 支付宝支付（原后端未实现支付回调，下单为模拟状态流转）
- 骑手端、商家移动端、消息推送 App
- 用户注册（用户表无密码字段，本期仅 mock 登录）
- 生产级运维（容器化、CI/CD 可作为后续简历加分项另立）

---

## 四、方案对比与选型

### 4.1 用户端网站登录：三方案对比（关键决策）

> 核心事实：`sky-pojo/entity/User.java` 只有 openid / name / phone / sex / idNumber / avatar / createTime，**无 username / password 字段**。现有 `/user/user/login` 接口接收微信 code，网站环境无法提供。因此用户端走网站登录必须改造。

| 方案 | 做法 | 后端改动量 | 学习偏离度 | 演示便利 | 评价 |
|------|------|-----------|-----------|----------|------|
| A（推荐） | 新增 `/user/user/mockLogin`，传手机号查 user 表，签发用户 JWT | 1 个 Controller 方法 + 1 个 Mapper 查询 | 极低 | 高 | 不动原有微信登录，改动最小，简历可讲"为演示环境设计隔离的测试登录入口" |
| B | user 表加 username/password 字段，实现注册+密码登录 | 改表 + 改实体 + 注册/登录接口 + 密码加密 | 中 | 高 | 改动大，但简历可讲"认证体系扩展"，时间充裕可选 |
| C | 前端硬编码 token 绕过登录 | 0 | 无 | 中 | 面试一问就穿帮，不推荐 |

**推荐 A**：`mockLogin` 加 `@Profile("dev")` 或独立路径隔离，避免污染学习主链路；面试时可明确说"生产环境仍走微信登录，测试入口仅用于本地演示"。

### 4.2 依赖就绪度评估

| 依赖项 | 就绪状态 | 分级 | 影响 | 替代方案 |
|--------|----------|------|------|----------|
| 管理端登录接口 | 已就绪 | 可绕过 | 无 | — |
| 用户端网站登录接口 | **未就绪** | **阻塞（Blocker）** | 用户端全部页面无法进入 | 方案 A：新增 mockLogin，最晚在前端用户端开发前完成 |
| OSS 图片（bucket=sky-itcast） | 待验证 | 并行 | 图片全部裂图 | 前端加 onerror 占位图兜底 |
| Redis（localhost:6379）/ MySQL（localhost:3306） | 本地环境待确认 | 阻塞 | 后端起不来 | 安装并启动服务，或改配置指向可用实例 |
| CORS 跨域 | 未配置 | 阻塞 | 前端请求被浏览器拦截 | Spring Boot 加 WebMvc CORS 配置（约 10 行） |

### 4.3 技术选型
- 管理端：Vue 3 + Vite + Element Plus + ECharts + Pinia + Vue Router
- 用户端：Vue 3 + Vite + Element Plus（与后台同栈，标准桌面网页平台）
- 统一门户：单页应用，登录成功后按 role 跳转 `/admin/*` 或 `/user/*`（可用 qiankun 做微前端，但面试更看重基础，本期不做）

---

## 五、用户角色与使用场景

| 角色 | 身份来源 | 核心目标 | 使用频率 |
|------|----------|----------|----------|
| 管理员 | employee 表（admin/123456） | 接单、维护菜品、看经营数据 | 每日 |
| 普通用户 | user 表（mockLogin 选定） | 点餐、下单、追踪订单 | 每次用餐 |

---

## 六、功能需求（逐页 + 接口映射）

> 状态词：必须（shall）/ 应该（should）/ 可以（may）/ 禁止（must not）。所有接口路径均从源码提取核对，非编造。

### FR-01 统一登录门户
- **描述**：单页同时提供身份切换与两套登录表单。
- **规则**：
  - 默认选中「管理员」；切换身份时清空已填表单。
  - 管理员表单提交至 `POST /admin/employee/login`，请求体 `{username, password}`。
  - 成功响应 `EmployeeLoginVO{id, userName, name, token}`；前端将 token 存 localStorage，以 header `token` 注入 axios 实例，跳转 `/admin/dashboard`。
  - 用户表单调用 mockLogin（见 FR-09），成功后以 header `authentication` 注入，跳转 `/user/home`。
  - 401 响应时展示"账号或密码错误"，禁止跳转。
- **验收（GWT）**：
  - Given 未登录，When 选「管理员」输入 admin/123456 提交，Then 进入工作台且请求头携带 token。
  - Given 切换到「用户」，When 提交手机号，Then 进入点餐首页且请求头携带 authentication。

### FR-02 管理端 · 工作台
- **接口**：`GET /admin/workspace/businessData`、`/overviewOrders`、`/overviewDishes`、`/overviewSetmeals`
- **规则**：4 个概览卡片 + 今日订单/菜品/套餐统计；ECharts 折线图调用 `GET /admin/report/turnoverStatistics?begin=&end=`（近 7 天）。
- **验收**：进入工作台 shall 展示 4 张卡片与折线图，数据非硬编码。

### FR-03 管理端 · 订单管理
- **接口**：`GET /admin/order/conditionSearch`（分页+条件）、`GET /admin/order/statistics`、`GET /admin/order/details/{id}`、`PUT /admin/order/confirm`、`/rejection`、`/cancel`、`/delivery/{id}`、`/complete/{id}`
- **规则**：状态 Tab（待接单/待派送/派送中/已完成/已取消）；状态统计角标；接单/拒单/派送/完成 shall 二次确认后调用。
- **验收**：点击「接单」并确认，Then 订单状态从"待接单"变为"待派送"，统计角标同步变化。

### FR-04 管理端 · 菜品 / 套餐 / 分类管理
- **接口**：
  - 菜品 `GET /admin/dish/page`、`POST /admin/dish`、`GET /admin/dish/{id}`、`PUT /admin/dish`、`DELETE /admin/dish`、`POST /admin/dish/status/{status}`、`GET /admin/dish/list`
  - 套餐 `GET /admin/setmeal/page`、`POST /admin/setmeal`、`GET /admin/setmeal/{id}`、`PUT /admin/setmeal`、`DELETE /admin/setmeal`、`POST /admin/setmeal/status/{status}`
  - 分类 `GET /admin/category/page`、`POST /admin/category`、`PUT /admin/category`、`DELETE /admin/category`、`POST /admin/category/status/{status}`、`GET /admin/category/list`
- **规则**：分页列表 + 新增/编辑抽屉表单 + 起售停售开关；图片走 `POST /admin/common/upload`。
- **验收**：新建菜品 shall 出现在列表首条；停售后用户端 `/user/dish/list` 不再返回该菜品。

### FR-05 管理端 · 数据统计大屏
- **接口**：`GET /admin/report/turnoverStatistics`、`/userStatistics`、`/ordersStatistics`、`/top10`、`/export`
- **规则**：4 类图表（营业额折线、用户增长双轴、订单量柱状、Top10 横向条形）+ 导出按钮下载 Excel。
- **验收**：切换日期范围 shall 重新请求接口并刷新图表；导出 shall 触发 .xlsx 下载。

### FR-06 用户端 · 点餐首页
- **接口**：`GET /user/shop/status`、`GET /user/category/list`、`GET /user/dish/list?categoryId=`、`GET /user/setmeal/list?categoryId=`
- **规则**：左侧分类 Tab + 右侧菜品/套餐列表；左侧"热销"固定；停售菜品由后端过滤不展示。
- **验收**：切换分类 shall 展示对应菜品；菜品图片加载失败时展示占位图。

### FR-07 用户端 · 菜品详情与购物车
- **接口**：`GET /user/setmeal/dish/{id}`（套餐详情）、`POST /user/shoppingCart/add`、`GET /user/shoppingCart/list`、`POST /user/shoppingCart/sub`、`DELETE /user/shoppingCart/clean`
- **规则**：菜品规格（口味/份数）在前端组装为 ShoppingCartDTO；购物车数量增减调用 add/sub；清空需二次确认。
- **验收**：加购 shall 使购物车角标 +1；同一菜品重复加购走数量累加（后端逻辑）。

### FR-08 用户端 · 确认下单与订单追踪
- **接口**：
  - 地址 `GET /user/addressBook/list`、`GET /user/addressBook/default`、`POST /user/addressBook`、`PUT /user/addressBook`、`PUT /user/addressBook/default`、`DELETE /user/addressBook?id=`、`GET /user/addressBook/{id}`
  - 下单 `POST /user/order/submit`
  - 订单 `GET /user/order/historyOrders`、`GET /user/order/orderDetail/{id}`、`PUT /user/order/cancel/{id}`、`POST /user/order/repetition/{id}`、`GET /user/order/reminder/{id}`
- **规则**：提交订单请求体 OrdersSubmitDTO；成功后展示订单号与模拟支付结果；催单调用 reminder（后端走 WebSocket 推送到管理端）。
- **验收**：提交订单 shall 生成订单号；在管理端订单列表 shall 能看到该订单；点击催单 shall 触发管理端提醒。

### FR-09（新增后端）用户端网站 Mock 登录
- **接口**：`POST /user/user/mockLogin`，请求体 `{phone}`，响应复用 `UserLoginVO{id, openid, token}`。
- **实现要点**：按 phone 查 user 表 → 存在则用 `user-secret-key` + `USER_ID` 签发 JWT（完全复用 UserController.login 的签发逻辑）→ 返回。加 `@Profile("dev")` 隔离。
- **拦截器**：`/user/user/mockLogin` 必须加入 `JwtTokenUserInterceptor` 的 excludePathPatterns（在 WebMvcConfiguration 第 50 行附近追加一行）。
- **验收**：传库中已有 phone shall 返回 token；该 token 调用 `/user/addressBook/list` shall 通过鉴权。

### FR-10（新增后端）CORS 跨域
- **实现**：在 WebMvcConfiguration 增加 `addCorsMappings`，允许 `localhost:5173/5174` 等前端端口，放行 token / authentication 请求头。
- **验收**：前端 dev server 请求后端 shall 不再出现 CORS 报错。

---

## 七、非功能需求

| 类别 | 要求 |
|------|------|
| 性能 | 列表页首屏数据请求 ≤ 1s（本地环境）；图表渲染不阻塞主界面 |
| 安全 | 前端不得硬编码密码；token 存 localStorage 且退出时清除；401 统一跳登录页 |
| 兼容 | Chrome / Edge 最近两个大版本；用户端为标准桌面网页平台布局，适配 1280px 及以上宽屏 |
| 可维护性 | axios 拦截器统一注入 token；API 层按 admin/user 分文件，与后端 Controller 一一对应 |

---

## 八、业务流程与状态流转

订单状态机（后端 OrderServiceImpl 已实现，前端只做展示与操作入口）：

```
待付款 → 待接单 → 已接单 → 派送中 → 已完成
            ↓
          已取消
```
- 用户可取消：待付款 / 待接单 / 已接单
- 管理员可：接单、拒单、取消、派送、完成
- 前端 shall 按当前状态只渲染允许的操作按钮，禁止越权操作入口。

---

## 九、权限与安全

| 角色 | 可访问前端 | 携带 token | 后端拦截器 | 数据范围 |
|------|-----------|-----------|-----------|----------|
| 管理员 | /admin/** | header `token`（admin-secret-key 签发，claims=empId） | JwtTokenAdminInterceptor | 全部店铺数据 |
| 用户 | /user/** | header `authentication`（user-secret-key 签发，claims=userId） | JwtTokenUserInterceptor | 仅本人（BaseContext 从 token 取 userId） |

**安全红线**：两套 token 不可混用；用户端前端禁止出现任何 /admin/** 接口调用；管理员端禁止调用 /user/user/mockLogin。

---

## 十、数据与埋点

本期为学习项目演示，不接入埋点 SDK。最小数据能力：管理端工作台展示 4 项实时指标（营业额、订单数、新增用户、客单价），数据口径均来自 `/admin/report/*` 接口，不造数。

---

## 十一、异常处理清单

| 场景 | 前端处理 |
|------|----------|
| 后端未启动 / 500 | axios 拦截器统一提示"服务暂不可用"，不白屏 |
| 401 token 过期 | 清除本地 token，跳转统一登录门户 |
| OSS 图片失效 | img onerror 切换本地占位图 |
| 购物车空 | 展示空状态插图 + "去点餐"按钮 |
| 订单列表空 | 展示空状态 + 分类 Tab 全选入口 |
| 提交订单重复点击 | 按钮 loading 禁用，防止重复提交（前端幂等） |

---

## 十二、上线计划与依赖

**MVP 顺序（按简历演示价值排序）**：
1. 后端最小改造（FR-09 mockLogin + FR-10 CORS）→ 阻塞项，最先做
2. 统一登录门户（FR-01）
3. 管理端工作台 + 订单管理（FR-02、FR-03）→ 形成"登录→看数→接单"闭环
4. 用户端点餐 + 下单（FR-06~FR-08）→ 形成完整交易闭环
5. 数据统计大屏（FR-05）→ 简历视觉亮点
6. 菜品/套餐/分类 CRUD（FR-04）→ 补齐管理端

**上线后验证清单**：
- [ ] 管理员登录成功进入工作台
- [ ] 用户登录成功进入点餐页
- [ ] 用户提交订单后管理端可见
- [ ] 管理端接单后用户端状态同步
- [ ] 图表数据随日期变化刷新

---

## 十三、风险与遗留问题

| 编号 | 风险 | 类别 | 概率×影响 | 等级 | 应对 |
|------|------|------|-----------|------|------|
| RK-01 | 用户表无密码字段，网站登录无法直接实现 | 技术 | 5×5=25 | 高 | 方案 A 新增 mockLogin（已列入本期） |
| RK-02 | OSS 桶 sky-itcast 过期导致图片全裂 | 技术 | 4×4=16 | 高 | 前端占位图兜底；或更换图床 |
| RK-03 | 无真实支付，简历表述容易被追问 | 需求 | 4×3=12 | 中 | 明确表述为"模拟下单状态流转"，不写"支付功能" |
| RK-04 | 后端本地环境（MySQL/Redis）未启动 | 技术 | 3×5=15 | 中高 | 开发前先起服务并联调 |
| RK-05 | 两套 JWT 混用导致越权 | 安全 | 2×5=10 | 中 | axios 按角色分实例，code review 检查 |

**遗留（后续版本）**：真实微信登录回填、WebSocket 来单提醒前端接入、Docker 一键部署、用户注册体系。

---

## 十四、附录

### 14.1 后端接口总清单（从源码提取，供前端建 API 层）

**管理端（46 个接口中的 admin 部分）**
- `/admin/employee`：login、logout、POST 新增、page、status/{status}、{id}、PUT 编辑
- `/admin/category`：POST、page、DELETE、PUT、status/{status}、list
- `/admin/dish`：POST、page、DELETE、{id}、PUT、status/{status}、list
- `/admin/setmeal`：POST、page、DELETE、{id}、PUT、status/{status}
- `/admin/order`：conditionSearch、statistics、details/{id}、confirm、rejection、cancel、delivery/{id}、complete/{id}
- `/admin/report`：turnoverStatistics、userStatistics、ordersStatistics、top10、export
- `/admin/shop`：PUT /{status}、GET /status
- `/admin/workspace`：businessData、overviewOrders、overviewDishes、overviewSetmeals
- `/admin/common`：upload

**用户端**
- `/user/user`：login、（新增）mockLogin
- `/user/shop`：status
- `/user/category`：list
- `/user/dish`：list
- `/user/setmeal`：list、dish/{id}
- `/user/shoppingCart`：add、list、clean、sub
- `/user/addressBook`：list、POST、{id}、PUT、default（PUT/GET）、DELETE
- `/user/order`：submit、historyOrders、orderDetail/{id}、cancel/{id}、repetition/{id}、reminder/{id}

### 14.2 需求追踪矩阵（RTM）

| 需求编号 | 来源 | PRD 章节 | 依赖接口 | 优先级 |
|----------|------|----------|----------|--------|
| FR-01 | 用户：统一入口选身份 | 六 | 两套 login | Must |
| FR-09 | 卡点：用户表无密码 | 四/六 | 新增 mockLogin | Must（阻塞） |
| FR-10 | 卡点：跨域 | 六 | CORS 配置 | Must（阻塞） |
| FR-02/03 | 简历：数据+订单亮点 | 六 | workspace/report/order | Must |
| FR-06/07/08 | 简历：交易闭环 | 六 | user 端全部 | Should |
| FR-05 | 简历：视觉亮点 | 六 | report 四接口 | Should |
| FR-04 | 完整性 | 六 | admin CRUD | Could |

### 14.3 合规声明
本方案为产品设计参考，具体实施请结合公司合规与法务要求确认。项目改造涉及的品牌名「味来 / Weilai」为虚构示例名，示例手机号、账号数据均已脱敏或为虚构值。
