package com.awe.foundation.common.api;

import com.awe.foundation.common.constant.ErrorCodeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;

/**
 * 通用响应结果
 *
 * @author Awe
 * @since 2025/9/9 11:04
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = -8419737474591126708L;

    /**
     * 响应码
     */
    private Integer code;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应消息
     */
    private String msg;

    public static <T> Result<T> success() {
        return new Result<>(ErrorCodeEnum.SUCCESS.getCode(), null, ErrorCodeEnum.SUCCESS.getMsg());
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ErrorCodeEnum.SUCCESS.getCode(), data, ErrorCodeEnum.SUCCESS.getMsg());
    }

    @SuppressWarnings("rawtypes")
    public static <T, R> Result success(T data, Function<T, R> function) {
        return new Result<>(ErrorCodeEnum.SUCCESS.getCode(), Objects.nonNull(data) ? function.apply(data) : null, ErrorCodeEnum.SUCCESS.getMsg());
    }

    public static <T> Result<T> failure() {
        return new Result<>(ErrorCodeEnum.FAILURE.getCode(), null, ErrorCodeEnum.FAILURE.getMsg());
    }

    public static <T> Result<T> failure(String msg) {
        return new Result<>(ErrorCodeEnum.FAILURE.getCode(), null, msg);
    }

    public static <T> Result<T> failure(int code, String msg) {
        return new Result<>(code, null, msg);
    }

    public static <T> Result<T> failure(ErrorCodeEnum codeEnum) {
        return new Result<>(codeEnum.getCode(), null, codeEnum.getMsg());
    }

    public static <T> Result<T> failure(ErrorCodeEnum codeEnum, String msg) {
        return new Result<>(codeEnum.getCode(), null, codeEnum.getMsg() + ": " + msg);
    }

    public static <T> Result<T> failure(String msg, T data) {
        return new Result<>(ErrorCodeEnum.FAILURE.getCode(), data, msg);
    }

    public static <T> Result<T> failure(int code, String msg, T data) {
        return new Result<>(code, data, msg);
    }

    @JsonIgnore
    public Boolean isError() {
        return !isSuccess();
    }

    @JsonIgnore
    public Boolean isSuccess() {
        return Objects.equals(ErrorCodeEnum.SUCCESS.getCode(), this.getCode());
    }

}
