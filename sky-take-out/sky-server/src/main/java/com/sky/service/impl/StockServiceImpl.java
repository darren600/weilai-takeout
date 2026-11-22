package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.exception.OrderBusinessException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 库存服务实现：用 Lua 脚本保证“判断库存 + 扣减”在 Redis 中原子执行，避免超卖。
 */
@Service
public class StockServiceImpl implements StockService {

    private static final String DISH_KEY = "weilai:stock:dish:";
    private static final String SETMEAL_KEY = "weilai:stock:setmeal:";

    private static final RedisScript<Long> DEDUCT_SCRIPT =
            RedisScript.of(new ClassPathResource("lua/deduct_stock.lua"), Long.class);

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void deduct(Long dishId, Long setmealId, Integer number) {
        if (dishId != null) {
            deductOne(DISH_KEY + dishId, dishId, number, true);
        }
        if (setmealId != null) {
            deductOne(SETMEAL_KEY + setmealId, setmealId, number, false);
        }
    }

    private void deductOne(String key, Long id, Integer number, boolean dish) {
        Long left = stringRedisTemplate.execute(DEDUCT_SCRIPT, List.of(key), String.valueOf(number));

        // -2 表示 Redis 尚未初始化该商品库存，从 DB 懒加载后再扣一次
        if (left != null && left == -2L) {
            Integer stock = dish
                    ? dishMapper.getById(id).getStock()
                    : setmealMapper.getById(id).getStock();
            if (stock == null) {
                stock = 0;
            }
            stringRedisTemplate.opsForValue().setIfAbsent(key, String.valueOf(stock));
            left = stringRedisTemplate.execute(DEDUCT_SCRIPT, List.of(key), String.valueOf(number));
        }

        if (left == null || left == -1L) {
            throw new OrderBusinessException(MessageConstant.STOCK_NOT_ENOUGH);
        }
    }

    @Override
    public void compensate(Long dishId, Long setmealId, Integer number) {
        if (dishId != null) {
            stringRedisTemplate.opsForValue().increment(DISH_KEY + dishId, number);
        }
        if (setmealId != null) {
            stringRedisTemplate.opsForValue().increment(SETMEAL_KEY + setmealId, number);
        }
    }
}
