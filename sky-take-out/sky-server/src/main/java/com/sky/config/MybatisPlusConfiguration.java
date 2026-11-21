package com.sky.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置：全工程唯一的分页插件。
 *
 * <p>分页唯一化：PageHelper 依赖已删除，分页统一由这里的
 * PaginationInnerInterceptor(MYSQL) 接管，避免两者并存导致
 * "total 正确但 records 重复"的经典问题。</p>
 */
@Configuration
public class MybatisPlusConfiguration {

    /**
     * 注册分页拦截器。
     *
     * @return MybatisPlusInterceptor 实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
