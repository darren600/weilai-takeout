package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Tag(name = "店铺相关接口")
@Slf4j
public class ShopController {
    public static final String KEY = "SHOP_STATUS";
    @Autowired
    private RedisTemplate redisTemplate;

    /*
     * 获取到店铺的营业状态
     * */
    @GetMapping("/status")
    @Operation(summary = "获取店铺的营业状态")
    public Result<Integer> getStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        // Redis 中可能还没有该 key（首次启动/缓存被清空），此时默认按营业中处理并回填，
        // 否则下面 status == 1 会对 null 拆箱抛 NPE，导致接口 500。
        if (status == null) {
            status = 1;
            redisTemplate.opsForValue().set(KEY, status);
        }
        log.info("获取店铺的营业状态:{}", status == 1 ? "营业中" : "打烊中");
        return Result.success(status);
    }


}
