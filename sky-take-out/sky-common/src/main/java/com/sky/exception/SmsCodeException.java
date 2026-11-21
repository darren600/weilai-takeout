package com.sky.exception;

/**
 * 短信验证码业务异常。
 *
 * <p>用于验证码过期、验证码错误、手机号格式非法等场景。
 * 继承 BaseException 后，由 GlobalExceptionHandler 统一转换为
 * Result{code:0, msg:异常信息}，前端拦截器会把 msg 用 ElMessage 提示。</p>
 */
public class SmsCodeException extends BaseException {

    public SmsCodeException(String msg) {
        super(msg);
    }
}
