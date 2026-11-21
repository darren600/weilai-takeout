package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.SendCodeDTO;
import com.sky.dto.SmsLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
import com.sky.service.SmsCodeService;
import com.sky.service.UserService;
import com.sky.utils.JwtUtil;
import com.sky.vo.UserLoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 手机号短信验证码登录 / 自动注册接口（演示用）。
 *
 * <p>与 MockLoginController 一致，用类级 @Profile("dev") 隔离，仅 dev 环境注册；
 * 无真实短信通道，验证码直接在 Result.data 回传供演示。
 * 复用用户端 JWT 约定：header=authentication、secret=itheima、claims key=userId、
 * 返回 UserLoginVO{id,openid,token}，前端零改动。</p>
 */
@RestController
@RequestMapping("/user/user")
@Profile("dev")
@Tag(name = "用户相关接口-短信登录")
@Slf4j
public class UserSmsLoginController {

    @Autowired
    private SmsCodeService smsCodeService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 发送短信验证码：生成 6 位验证码存 Redis（TTL 300s），dev 环境直接回传验证码。
     *
     * @param sendCodeDTO 入参，含 phone
     * @return 验证码字符串
     */
    @PostMapping("/sendCode")
    @Operation(summary = "发送短信验证码（dev环境回传验证码）")
    public Result<String> sendCode(@RequestBody SendCodeDTO sendCodeDTO) {
        String code = smsCodeService.generateAndCache(sendCodeDTO.getPhone());
        return Result.success(code);
    }

    /**
     * 短信验证码登录：校验验证码（手机号不存在则自动注册），签发用户端 JWT。
     *
     * @param smsLoginDTO 入参，含 phone、code
     * @return UserLoginVO{id, openid, token}
     */
    @PostMapping("/smsLogin")
    @Operation(summary = "短信验证码登录/自动注册（仅dev环境）")
    public Result<UserLoginVO> smsLogin(@RequestBody SmsLoginDTO smsLoginDTO) {
        // 1. 校验验证码并登录（内部按需自动注册）
        User user = userService.smsLogin(smsLoginDTO);

        // 2. 复用用户端 JWT 签发逻辑（claims=userId, secret=itheima, ttl 不变）
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getUserSecretKey(),
                jwtProperties.getUserTtl(),
                claims);

        // 3. 组装 VO 返回，结构沿用 UserLoginVO{id,openid,token}
        UserLoginVO userLoginVO = UserLoginVO.builder()
                .id(user.getId())
                .openid(user.getOpenid())
                .token(token)
                .build();
        return Result.success(userLoginVO);
    }
}
