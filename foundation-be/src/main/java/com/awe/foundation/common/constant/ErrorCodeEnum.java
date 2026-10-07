package com.awe.foundation.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 错误编码枚举
 *
 * @author Awe
 * @since 2025/9/9 11:21
 */
@Getter
@AllArgsConstructor
public enum ErrorCodeEnum {

    // 成功
    SUCCESS(0, "成功"),

    // 失败
    FAILURE(9000, "失败"),

    // 异常
    ERROR(9999, "未知异常，请联系管理员"),

    // 系统异常
    SYSTEM_ERROR(500, "系统异常，请联系管理员"),

    // 参数校验失败
    PARAMETER_ERROR(9001, "参数校验失败"),

    // 请求过于频繁
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),

    // 请求体无法记录
    REQUEST_BODY_NOT_LOGGED(10001, "请求体未记录"),

    // 用户不存在
    USER_NOT_FOUND(10002, "用户不存在"),

    // 用户密码错误
    PASSWORD_ERROR(10004, "用户名或密码错误"),

    // 用户已禁用
    USER_DISABLED(10005, "用户已禁用"),

    // 原密码错误
    OLD_PASSWORD_ERROR(10006, "原密码错误"),

    // 数据不存在
    DATA_NOT_FOUND(10007, "数据不存在"),

    // 系统内置数据不可修改
    SYSTEM_DATA_PROTECTED(10008, "系统内置数据不可修改"),

    // 关联数据已禁用
    DATA_DISABLED(10009, "关联数据已禁用"),

    // 未登录
    NOT_LOGIN(401, "请先登录"),

    // 无权限
    NO_PERMISSION(403, "无权限访问"),

    // 不支持的认证类型
    AUTH_TYPE_UNSUPPORTED(10003, "不支持的认证类型"),
    ;

    /**
     * 错误编码
     */
    private final Integer code;

    /**
     * 错误信息
     */
    private final String msg;

    // 防止code码重复
    static {
        long distinctCount = Arrays.stream(values()).map(ErrorCodeEnum::getCode).distinct().count();
        if (distinctCount != (long) values().length) {
            throw new IllegalArgumentException("duplicate code in BizCodeEnum");
        }
    }

}
