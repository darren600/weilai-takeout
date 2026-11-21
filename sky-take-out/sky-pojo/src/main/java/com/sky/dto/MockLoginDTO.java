package com.sky.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 网站登录入参（mockLogin）。
 *
 * <p>用户表没有密码字段，网站登录走手机号匹配的演示方案：
 * 前端只传 phone，后端按 phone 查库并签发用户端 JWT。</p>
 */
@Data
public class MockLoginDTO implements Serializable {

    /** 手机号，对应用户表的 phone 字段（唯一键）。 */
    private String phone;
}
