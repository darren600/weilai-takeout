package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 发送短信验证码入参（演示用）。
 *
 * <p>用户只需提供手机号，后端生成验证码并写入 Redis；
 * dev 环境为方便演示，会把验证码直接通过 Result.data 回传。</p>
 */
@Data
public class SendCodeDTO implements Serializable {

    /** 手机号（1 开头共 11 位）。 */
    private String phone;
}
