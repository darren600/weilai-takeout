package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sky.constant.MessageConstant;
import com.sky.dto.MockLoginDTO;
import com.sky.dto.SmsLoginDTO;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.mapper.mp.UserMpMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.SmsCodeService;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    public static final String WX_LOGIN = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    /** MyBatis-Plus 隔离接口，仅用于 mockLogin 按 phone 查询 */
    @Autowired
    private UserMpMapper userMpMapper;

    /** 短信验证码服务：负责校验并消费验证码 */
    @Autowired
    private SmsCodeService smsCodeService;
    /*
    * 微信登录
    * @param userLoginDTO 用户登录参数
    * @return 用户信息
    * */


    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
//        调用微信接口服务，获得当前微信用户的openid
        String openid = getOpenid(userLoginDTO.getCode());

//        判断openid是否为空，如果为空表示登录失败，抛出业务异常

        if(openid == null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
//        判断当前用户是否为新用户
        User user = userMapper.getByOpenid(openid);

//        如果是新用户，自动完成注册
        if(user == null){
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
//        返回这个用户对象
        return user;


    }
    /**
     * 网站登录（演示用）：用 MyBatis-Plus 的 LambdaQueryWrapper 按手机号查用户。
     *
     * @param mockLoginDTO 入参，含 phone
     * @return 命中的用户
     */
    @Override
    public User mockLogin(MockLoginDTO mockLoginDTO) {
        log.info("网站登录手机号：{}", mockLoginDTO.getPhone());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, mockLoginDTO.getPhone()).last("limit 1");
        User user = userMpMapper.selectOne(wrapper);
        if (user == null) {
            // 复用现有登录失败异常，GlobalExceptionHandler 会转成 Result{code:0, msg:"登录失败"}
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        return user;
    }

    /**
     * 短信验证码登录（演示用）：先校验并消费验证码，再按手机号查用户；
     * 手机号不存在则自动注册（无密码字段，仅填手机号/昵称/注册时间）。
     *
     * @param smsLoginDTO 入参，含 phone、code
     * @return 登录或自动注册后的用户
     */
    @Override
    public User smsLogin(SmsLoginDTO smsLoginDTO) {
        // 1. 校验并消费验证码（过期/错误会抛 SmsCodeException，由全局处理器转 Result{code:0}）
        smsCodeService.verifyAndConsume(smsLoginDTO.getPhone(), smsLoginDTO.getCode());

        // 2. 按手机号查用户（复用隔离的 MP Mapper，与 mockLogin 一致）
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, smsLoginDTO.getPhone()).last("limit 1");
        User user = userMpMapper.selectOne(wrapper);

        // 3. 新用户自动注册：无密码，手机号 + 昵称（味来用户+后4位）+ 注册时间
        if (user == null) {
            String phone = smsLoginDTO.getPhone();
            String suffix = phone.length() >= 4 ? phone.substring(phone.length() - 4) : phone;
            user = User.builder()
                    .phone(phone)
                    .name("味来用户" + suffix)
                    .createTime(LocalDateTime.now())
                    .build();
            userMpMapper.insert(user);
            log.info("短信登录自动注册新用户 id={}, phone={}", user.getId(), phone);
        }
        return user;
    }

    private String getOpenid(String code){
        Map<String,String> map = new HashMap<>();
        map.put("appid",weChatProperties.getAppid());
        map.put("secret",weChatProperties.getSecret());
//        js_code表示登录的时候的code
        map.put("js_code",code);
        map.put("grant_type","authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN, map);

        JSONObject jsonObject = JSON.parseObject(json);
        String openid = jsonObject.getString("openid");
        return openid;
    }
}
