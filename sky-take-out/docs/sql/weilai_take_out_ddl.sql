-- ============================================================
-- 味来 Weilai 建库脚本（源自 PRD v2.0 第四章 4.4）
-- 表名、列名与原库 sky_take_out 完全一致，Mapper XML 零改动即可联调。
-- 使用方式：MySQL 8.x 执行本脚本；若要沿用旧库名，把第 5 行改为 sky_take_out。
-- 字符集合一 utf8mb4；主键 BIGINT AUTO_INCREMENT；金额 DECIMAL(10,2)。
-- ============================================================
CREATE DATABASE IF NOT EXISTS weilai_take_out
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE weilai_take_out;

-- 1. 员工表
CREATE TABLE employee (
  id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  username     VARCHAR(64)  NOT NULL                COMMENT '登录账号',
  name         VARCHAR(64)  NOT NULL                COMMENT '姓名',
  password     VARCHAR(128) NOT NULL                COMMENT '密码(MD5)',
  phone        VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
  sex          VARCHAR(4)   DEFAULT NULL            COMMENT '性别',
  id_number    VARCHAR(32)  DEFAULT NULL            COMMENT '身份证号',
  status       TINYINT      NOT NULL DEFAULT 1      COMMENT '0禁用 1启用',
  create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_user  BIGINT       DEFAULT NULL            COMMENT '创建人',
  update_user  BIGINT       DEFAULT NULL            COMMENT '修改人',
  PRIMARY KEY (id),
  UNIQUE KEY uk_username (username),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工/管理员表';

-- 2. 用户表（无密码字段，网站登录走 mockLogin）
CREATE TABLE user (
  id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  openid      VARCHAR(64)  DEFAULT NULL            COMMENT '微信openid',
  name        VARCHAR(64)  DEFAULT NULL            COMMENT '姓名',
  phone       VARCHAR(20)  DEFAULT NULL            COMMENT '手机号(mockLogin查询键)',
  sex         VARCHAR(4)   DEFAULT NULL            COMMENT '0女 1男',
  id_number   VARCHAR(32)  DEFAULT NULL            COMMENT '身份证号',
  avatar      VARCHAR(512) DEFAULT NULL            COMMENT '头像URL',
  create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_openid (openid),
  UNIQUE KEY uk_phone (phone),
  KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 3. 分类表
CREATE TABLE category (
  id          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
  type        TINYINT     NOT NULL                COMMENT '1菜品分类 2套餐分类',
  name        VARCHAR(64) NOT NULL                COMMENT '分类名称',
  sort        INT         NOT NULL DEFAULT 0      COMMENT '排序号',
  status      TINYINT     NOT NULL DEFAULT 1      COMMENT '0禁用 1启用',
  create_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_user BIGINT      DEFAULT NULL            COMMENT '创建人',
  update_user BIGINT      DEFAULT NULL            COMMENT '修改人',
  PRIMARY KEY (id),
  KEY idx_type_sort (type, sort),
  KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- 4. 菜品表
CREATE TABLE dish (
  id          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
  name        VARCHAR(128)   NOT NULL                COMMENT '菜品名称',
  category_id BIGINT         NOT NULL                COMMENT '分类ID',
  price       DECIMAL(10,2)  NOT NULL                COMMENT '菜品价格',
  image       VARCHAR(512)   DEFAULT NULL            COMMENT '图片URL',
  description VARCHAR(512)   DEFAULT NULL            COMMENT '描述',
  status      TINYINT        NOT NULL DEFAULT 1      COMMENT '0停售 1起售',
  create_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_user BIGINT         DEFAULT NULL            COMMENT '创建人',
  update_user BIGINT         DEFAULT NULL            COMMENT '修改人',
  PRIMARY KEY (id),
  KEY idx_cat_status (category_id, status),
  KEY idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品表';

-- 5. 菜品口味表
CREATE TABLE dish_flavor (
  id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  dish_id BIGINT       NOT NULL                COMMENT '菜品ID',
  name    VARCHAR(64)  NOT NULL                COMMENT '口味名(如辣度)',
  value   VARCHAR(512) NOT NULL                COMMENT '口味值JSON数组',
  PRIMARY KEY (id),
  KEY idx_dish_id (dish_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品口味表';

-- 6. 套餐表
CREATE TABLE setmeal (
  id          BIGINT         NOT NULL AUTO_INCREMENT COMMENT '主键',
  category_id BIGINT         NOT NULL                COMMENT '分类ID',
  name        VARCHAR(128)   NOT NULL                COMMENT '套餐名称',
  price       DECIMAL(10,2)  NOT NULL                COMMENT '套餐价格',
  status      TINYINT        NOT NULL DEFAULT 1      COMMENT '0停用 1启用',
  description VARCHAR(512)   DEFAULT NULL            COMMENT '描述',
  image       VARCHAR(512)   DEFAULT NULL            COMMENT '图片URL',
  create_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  create_user BIGINT         DEFAULT NULL            COMMENT '创建人',
  update_user BIGINT         DEFAULT NULL            COMMENT '修改人',
  PRIMARY KEY (id),
  KEY idx_cat_status (category_id, status),
  KEY idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐表';

-- 7. 套餐菜品关联表
CREATE TABLE setmeal_dish (
  id         BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  setmeal_id BIGINT        NOT NULL                COMMENT '套餐ID',
  dish_id    BIGINT        NOT NULL                COMMENT '菜品ID',
  name       VARCHAR(128)  NOT NULL                COMMENT '菜品名快照',
  price      DECIMAL(10,2) NOT NULL                COMMENT '菜品原价快照',
  copies     INT           NOT NULL DEFAULT 1      COMMENT '份数',
  PRIMARY KEY (id),
  KEY idx_setmeal_id (setmeal_id),
  KEY idx_dish_id (dish_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐菜品关联表';

-- 8. 订单主表（支付字段仅保留兼容，无真实支付流程）
CREATE TABLE orders (
  id                       BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  number                   VARCHAR(64)   NOT NULL                COMMENT '订单号',
  status                   TINYINT       NOT NULL DEFAULT 2      COMMENT '1待付款 2待接单 3已接单 4派送中 5已完成 6已取消 7退款',
  user_id                  BIGINT        NOT NULL                COMMENT '下单用户ID',
  address_book_id          BIGINT        NOT NULL                COMMENT '地址ID',
  order_time               DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  checkout_time            DATETIME      DEFAULT NULL            COMMENT '结账时间',
  pay_method               TINYINT       DEFAULT NULL            COMMENT '保留字段:1微信 2支付宝;本轮无真实支付',
  pay_status               TINYINT       DEFAULT NULL            COMMENT '保留字段:0未支付 1已支付 2退款;本轮不产生真实支付状态',
  amount                   DECIMAL(10,2) NOT NULL                COMMENT '实收金额',
  remark                   VARCHAR(512)  DEFAULT NULL            COMMENT '备注',
  user_name                VARCHAR(64)   DEFAULT NULL            COMMENT '用户名快照',
  phone                    VARCHAR(20)   DEFAULT NULL            COMMENT '手机号快照',
  address                  VARCHAR(512)  DEFAULT NULL            COMMENT '地址快照',
  consignee                VARCHAR(64)   DEFAULT NULL            COMMENT '收货人快照',
  cancel_reason            VARCHAR(256)  DEFAULT NULL            COMMENT '取消原因',
  rejection_reason         VARCHAR(256)  DEFAULT NULL            COMMENT '拒单原因',
  cancel_time              DATETIME      DEFAULT NULL            COMMENT '取消时间',
  estimated_delivery_time  DATETIME      DEFAULT NULL            COMMENT '预计送达时间',
  delivery_status          TINYINT       DEFAULT NULL            COMMENT '1立即送出 0指定时间',
  delivery_time            DATETIME      DEFAULT NULL            COMMENT '送达时间',
  pack_amount              INT           NOT NULL DEFAULT 0      COMMENT '打包费',
  tableware_number         INT           NOT NULL DEFAULT 0      COMMENT '餐具数量',
  tableware_status         TINYINT       DEFAULT NULL            COMMENT '1按餐量提供 0指定数量',
  PRIMARY KEY (id),
  UNIQUE KEY uk_number (number),
  KEY idx_user_status (user_id, status),
  KEY idx_status_ordertime (status, order_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';

-- 9. 订单明细表
CREATE TABLE order_detail (
  id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  name        VARCHAR(128)  NOT NULL                COMMENT '商品名快照',
  order_id    BIGINT        NOT NULL                COMMENT '订单ID',
  dish_id     BIGINT        DEFAULT NULL            COMMENT '菜品ID',
  setmeal_id  BIGINT        DEFAULT NULL            COMMENT '套餐ID',
  dish_flavor VARCHAR(256)  DEFAULT NULL            COMMENT '口味快照',
  number      INT           NOT NULL DEFAULT 1      COMMENT '数量',
  amount      DECIMAL(10,2) NOT NULL                COMMENT '金额',
  image       VARCHAR(512)  DEFAULT NULL            COMMENT '图片快照',
  PRIMARY KEY (id),
  KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- 10. 购物车表
CREATE TABLE shopping_cart (
  id          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
  name        VARCHAR(128)  NOT NULL                COMMENT '商品名',
  user_id     BIGINT        NOT NULL                COMMENT '用户ID',
  dish_id     BIGINT        DEFAULT NULL            COMMENT '菜品ID',
  setmeal_id  BIGINT        DEFAULT NULL            COMMENT '套餐ID',
  dish_flavor VARCHAR(256)  DEFAULT NULL            COMMENT '口味',
  number      INT           NOT NULL DEFAULT 1      COMMENT '数量',
  amount      DECIMAL(10,2) NOT NULL                COMMENT '单价',
  image       VARCHAR(512)  DEFAULT NULL            COMMENT '图片',
  create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  PRIMARY KEY (id),
  KEY idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 11. 地址簿表
CREATE TABLE address_book (
  id            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  user_id       BIGINT       NOT NULL                COMMENT '用户ID',
  consignee     VARCHAR(64)  NOT NULL                COMMENT '收货人',
  phone         VARCHAR(20)  NOT NULL                COMMENT '手机号',
  sex           VARCHAR(4)   DEFAULT NULL            COMMENT '0女 1男',
  province_code VARCHAR(32)  DEFAULT NULL            COMMENT '省级区划编号',
  province_name VARCHAR(64)  DEFAULT NULL            COMMENT '省级名称',
  city_code     VARCHAR(32)  DEFAULT NULL            COMMENT '市级区划编号',
  city_name     VARCHAR(64)  DEFAULT NULL            COMMENT '市级名称',
  district_code VARCHAR(32)  DEFAULT NULL            COMMENT '区级区划编号',
  district_name VARCHAR(64)  DEFAULT NULL            COMMENT '区级名称',
  detail        VARCHAR(512) NOT NULL                COMMENT '详细地址',
  label         VARCHAR(32)  DEFAULT NULL            COMMENT '标签(家/公司)',
  is_default    TINYINT      NOT NULL DEFAULT 0      COMMENT '0否 1是',
  create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间(新增列,可选)',
  update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间(新增列,可选)',
  PRIMARY KEY (id),
  KEY idx_user_default (user_id, is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地址簿表';
