package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.exception.SmsCodeException;
import com.sky.service.SmsCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * 短信验证码服务实现（演示用，无真实短信通道）。
 *
 * <p>为什么用 StringRedisTemplate 而不是项目里那个原始 RedisTemplate：
 * 现有 RedisConfiguration 里的 RedisTemplate 未设置 value 序列化器，value 走 JDK 序列化，
 * 存进去是二进制、redis-cli 里不可读；StringRedisTemplate 存纯字符串，便于演示与排查。
 * 这里刻意不改动全局 RedisTemplate 的 value 序列化器，避免影响现有 SHOP_STATUS(Integer) 的读取。</p>
 */
@Service
@Slf4j
public class SmsCodeServiceImpl implements SmsCodeService {

    /** Redis key 前缀（用户明确要求：weilai:sms:code:{phone}）。 */
    private static final String CODE_KEY_PREFIX = "weilai:sms:code:";

    /** 验证码有效期：300 秒（5 分钟）。 */
    private static final Duration CODE_TTL = Duration.ofSeconds(300);

    /** 手机号格式：1 开头，共 11 位。 */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");

    /** 验证码取值上限（6 位：0 ~ 999999）。 */
    private static final int CODE_BOUND = 1_000_000;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String generateAndCache(String phone) {
        validatePhone(phone);
        // 生成 6 位数字验证码，左补零保证长度一致（如 000123）
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(CODE_BOUND));
        stringRedisTemplate.opsForValue().set(buildKey(phone), code, CODE_TTL);
        // 无真实短信通道：dev 环境仅打印日志，验证码通过接口返回值交给前端演示
        log.info("生成短信验证码 phone={}, code={}, ttl={}s", phone, code, CODE_TTL.getSeconds());
        return code;
    }

    @Override
    public void verifyAndConsume(String phone, String code) {
        validatePhone(phone);
        String key = buildKey(phone);
        String cachedCode = stringRedisTemplate.opsForValue().get(key);
        if (cachedCode == null) {
            // key 不存在或已过期
            throw new SmsCodeException(MessageConstant.SMS_CODE_EXPIRED);
        }
        if (!cachedCode.equals(code)) {
            throw new SmsCodeException(MessageConstant.SMS_CODE_ERROR);
        }
        // 校验通过后立即删除，保证一次性使用、防重放
        stringRedisTemplate.delete(key);
    }

    /**
     * 校验手机号格式，非法则抛业务异常。
     *
     * @param phone 手机号
     */
    private void validatePhone(String phone) {
        if (phone == null || !PHONE_PATTERN.matcher(phone).matches()) {
            throw new SmsCodeException(MessageConstant.PHONE_FORMAT_ERROR);
        }
    }

    /**
     * 拼接 Redis key：weilai:sms:code:{phone}。
     *
     * @param phone 手机号
     * @return 完整 key
     */
    private String buildKey(String phone) {
        return CODE_KEY_PREFIX + phone;
    }
}
