package com.sky.service;

/**
 * 库存服务：下单时原子扣减，DB 失败时补偿回滚。
 */
public interface StockService {

    /**
     * 扣减库存
     *
     * @param dishId    菜品id，套餐明细时为 null
     * @param setmealId 套餐id，菜品明细时为 null
     * @param number    购买数量
     */
    void deduct(Long dishId, Long setmealId, Integer number);

    /**
     * 补偿库存（DB 事务失败时把 Redis 库存加回去）
     */
    void compensate(Long dishId, Long setmealId, Integer number);
}
