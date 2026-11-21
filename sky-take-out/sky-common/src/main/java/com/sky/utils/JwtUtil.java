package com.sky.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类（JJWT 0.11.5 版本）。
 *
 * <p>升级要点（坑 B）：JJWT 0.11.5 对 HS256 强制要求密钥长度 ≥ 32 字节，
 * 而配置里的 secret 是 itcast(6 字节) / itheima(7 字节)，直接用会抛 WeakKeyException。
 * 我们的做法是：配置字面保持不变（前端零改动），在 {@link #keyOf(String)} 内部
 * 用确定性函数把短密钥补齐到 32 字节。签发与解析都走同一个方法，所以自洽。</p>
 *
 * <p>0.11.5 API 提示：签名用 signWith(SecretKey, SignatureAlgorithm.HS256)，
 * 解析用 Jwts.parserBuilder()；架构文档里出现的 Jwts.SIG / parser().verifyWith()
 * 是 0.12.x 写法，0.11.5 并不存在，故此处按 0.11.5 实现。</p>
 *
 * <p>副作用：升级前签发的旧 token 无法再解析，需要重新登录一次，演示项目可接受。</p>
 */
public class JwtUtil {

    /** HS256 要求密钥至少 32 字节（256 bit）。 */
    private static final int MIN_KEY_BYTES = 32;

    /**
     * 把短密钥确定性地补齐到 32 字节，生成 HS256 可用的 SecretKey。
     *
     * <p>采用 Arrays.copyOf 右侧补 0 的方式：不足 32 字节补 0，超过则截断。
     * 这是确定性的——同一个 secret 每次都得到同一把 key，因此 createJWT 签发的 token
     * 能被 parseJWT 用同样的 secret 正确解析。对演示项目足够安全，且对初学者最直观。</p>
     *
     * @param secret 配置中的短密钥（itcast / itheima）
     * @return 32 字节的 HS256 密钥
     */
    private static SecretKey keyOf(String secret) {
        byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
        byte[] keyBytes = Arrays.copyOf(raw, MIN_KEY_BYTES);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成 jwt，使用 HS256 算法。
     *
     * @param secretKey jwt 秘钥
     * @param ttlMillis jwt 过期时间（毫秒）
     * @param claims    私有声明（管理员放 empId，用户放 userId）
     * @return 签名后的 jwt 字符串
     */
    public static String createJWT(String secretKey, long ttlMillis, Map<String, Object> claims) {
        // 过期时间 = 当前时间 + ttl
        Instant expInstant = Instant.ofEpochMilli(System.currentTimeMillis() + ttlMillis);
        Date exp = Date.from(expInstant);

        // 0.11.x 通用链式 API：先放私有声明，再放过期时间，最后指定密钥与算法。
        // 用 setClaims/setExpiration 而非 claims/expiration，兼容 0.11.x 各小版本。
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(exp)
                .signWith(keyOf(secretKey), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析 jwt 并校验签名，签名不符或已过期都会抛异常。
     *
     * @param secretKey jwt 秘钥（服务端保管，不能泄露）
     * @param token     加密后的 token
     * @return token 中的声明
     */
    public static Claims parseJWT(String secretKey, String token) {
        // 0.11.x 通用：setSigningKey(SecretKey) + parseClaimsJws().getBody()。
        // 不用 verifyWith/parseSignedClaims/getPayload（那是 0.12.x 写法）。
        return Jwts.parserBuilder()
                .setSigningKey(keyOf(secretKey))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
