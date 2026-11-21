package com.sky.service;

/**
 * 短信验证码服务（演示用，无真实短信通道）。
 *
 * <p>负责验证码的生成、Redis 缓存与一次性校验消费。
 * 验证码存储规范：key = weilai:sms:code:{phone}，TTL = 300 秒（5 分钟）。</p>
 */
public interface SmsCodeService {

    /**
     * 生成 6 位数字验证码并写入 Redis，返回验证码本身（供 dev 环境演示直接回传）。
     *
     * @param phone 手机号（1 开头 11 位）
     * @return 生成的 6 位验证码
     */
    String generateAndCache(String phone);

    /**
     * 校验验证码并消费（一次性使用，防重放）。
     *
     * <p>不存在或已过期抛 {@code SmsCodeException("验证码已过期，请重新获取")}；
     * 不匹配抛 {@code SmsCodeException("验证码错误")}；校验通过后立即删除该 key。</p>
     *
     * @param phone 手机号
     * @param code  用户输入的验证码
     */
    void verifyAndConsume(String phone, String code);
}
