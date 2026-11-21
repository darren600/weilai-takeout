package com.sky.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
public class RedisConfiguration {
//    RedisConnectionFactory这个连接工厂对象时spring引入spring Data Redis时自动生成的，并放到spring容器当中，用于创建RedisTemplate对象
    @Bean//为什么要加Bean，因为spring容器当中没有RedisTemplate对象，所以要加Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
        log.info("开始创建redis模板对象。。。");
//        创建对象
        RedisTemplate redisTemplate = new RedisTemplate();
//        设置连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);
//        设置redis key序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());
//        设置redis value序列化器-防止乱码
        //redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}
