package com.sky.controller.user;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.MockLoginDTO;
import com.sky.entity.User;
import com.sky.properties.JwtProperties;
import com.sky.result.Result;
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
 * 网站登录接口（演示用，mockLogin）。
 *
 * <p>用独立的 Controller + 类级 @Profile("dev") 隔离：
 * 只有 dev 环境（application-dev.yml）才注册这个 Bean，
 * prod 环境该接口不存在，避免演示后门泄漏到生产。
 * 之所以不用方法级 @Profile——它对 @Controller 方法默认不生效，类级最稳妥。</p>
 *
 * <p>URL 与登录逻辑沿用用户端约定：header=authentication、secret=itheima、
 * claims key=userId、返回 UserLoginVO{id,openid,token}，前端零改动。</p>
 */
@RestController
@RequestMapping("/user/user")
@Profile("dev")
@Tag(name = "用户相关接口-演示登录")
@Slf4j
public class MockLoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 网站登录：按手机号查用户并签发用户端 JWT。
     *
     * @param mockLoginDTO 入参，含 phone
     * @return UserLoginVO{id, openid, token}
     */
    @PostMapping("/mockLogin")
    @Operation(summary = "网站登录（手机号，仅dev环境）")
    public Result<UserLoginVO> mockLogin(@RequestBody MockLoginDTO mockLoginDTO) {
        // 1. 按手机号查用户，查不到抛 LoginFailedException -> Result{code:0}
        User user = userService.mockLogin(mockLoginDTO);

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
