package com.sky.service;

import com.sky.dto.MockLoginDTO;
import com.sky.dto.SmsLoginDTO;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

public interface UserService {
    User wxLogin(UserLoginDTO userLoginDTO);

    /**
     * 网站登录（演示用）：按手机号查用户，查不到抛业务异常。
     *
     * @param mockLoginDTO 入参，含 phone
     * @return 命中的用户
     */
    User mockLogin(MockLoginDTO mockLoginDTO);

    /**
     * 短信验证码登录（演示用）：校验验证码后按手机号查用户，不存在则自动注册再登录。
     *
     * @param smsLoginDTO 入参，含 phone、code
     * @return 登录或自动注册后的用户
     */
    User smsLogin(SmsLoginDTO smsLoginDTO);
}
