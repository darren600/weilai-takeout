-- 2026-11-22 库存字段迁移：给现有库补 stock 列
-- 新库请使用更新后的 docs/sql/weilai_take_out_ddl.sql
ALTER TABLE dish ADD COLUMN stock INT NOT NULL DEFAULT 100 COMMENT '库存' AFTER status;
ALTER TABLE setmeal ADD COLUMN stock INT NOT NULL DEFAULT 100 COMMENT '库存' AFTER status;

-- Redis 库存 key（weilai:stock:dish:{id} / weilai:stock:setmeal:{id}）
-- 会在首次下单时从 DB 懒加载初始化，无需手工写入。
