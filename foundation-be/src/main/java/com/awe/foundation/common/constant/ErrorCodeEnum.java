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

    SUCCESS(0, "成功"),

    // 通用错误（100xxx）
    FAILURE(100000, "失败"),
    PARAMETER_ERROR(100001, "参数校验失败"),
    REQUEST_BODY_NOT_LOGGED(100002, "请求体未记录"),

    // 认证授权（200xxx）
    NOT_LOGIN(200001, "请先登录"),
    NO_PERMISSION(200002, "无权限访问"),
    AUTH_TYPE_UNSUPPORTED(200003, "不支持的认证类型"),

    // 用户（300xxx）
    USER_NOT_FOUND(300001, "用户不存在"),
    PASSWORD_ERROR(300002, "用户名或密码错误"),
    USER_DISABLED(300003, "用户已禁用"),
    OLD_PASSWORD_ERROR(300004, "原密码错误"),

    // 数据（400xxx）
    DATA_NOT_FOUND(400001, "数据不存在"),
    SYSTEM_DATA_PROTECTED(400002, "系统内置数据不可修改"),
    DATA_DISABLED(400003, "关联数据已禁用"),
    UNIQUE_CONSTRAINT_VIOLATION(400004, "数据已存在，请勿重复提交"),
    OPTIMISTIC_LOCK_CONFLICT(400005, "数据已被其他操作修改，请刷新后重试"),

    // 请求限流（429xxx）
    TOO_MANY_REQUESTS(429001, "请求过于频繁，请稍后再试"),

    // 系统异常（500xxx）
    SYSTEM_ERROR(500001, "系统异常，请联系管理员"),
    ERROR(500999, "未知异常，请联系管理员"),

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
            throw new IllegalArgumentException("duplicate code in ErrorCodeEnum");
        }
        boolean invalidCode = Arrays.stream(values())
                .filter(errorCode -> !SUCCESS.equals(errorCode))
                .anyMatch(errorCode -> errorCode.getCode() < 100000 || errorCode.getCode() > 999999);
        if (invalidCode) {
            throw new IllegalArgumentException("business code must be a six-digit number except SUCCESS");
        }
    }

}
