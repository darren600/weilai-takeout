package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 短信验证码登录入参（演示用）。
 *
 * <p>后端校验验证码通过后按手机号查用户；手机号不存在则自动注册再登录。
 * 用户表无密码字段，所以无需密码相关字段。</p>
 */
@Data
public class SmsLoginDTO implements Serializable {

    /** 手机号（1 开头共 11 位）。 */
    private String phone;

    /** 短信验证码（6 位数字）。 */
    private String code;
}
