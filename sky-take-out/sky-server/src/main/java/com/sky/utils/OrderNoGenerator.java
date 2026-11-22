package com.sky.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 基于 Redis INCR 的分布式订单号生成器。
 * 格式：yyyyMMdd + 6位自增序列，例如 20261122000001
 */
@Component
public class OrderNoGenerator {

    private static final String KEY_PREFIX = "weilai:order:no:";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public String nextNumber() {
        String date = LocalDate.now().format(DATE_FMT);
        String key = KEY_PREFIX + date;

        // INCR 是原子操作，多实例并发也不会拿到相同值
        Long seq = stringRedisTemplate.opsForValue().increment(key);

        // 只在第一次递增时设过期，避免每次请求都刷新 TTL
        if (seq != null && seq == 1L) {
            stringRedisTemplate.expire(key, Duration.ofHours(48));
        }

        return date + String.format("%06d", seq);
    }
}
