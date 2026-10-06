package com.awe.foundation.common.api;

import com.awe.foundation.common.constant.ErrorCodeEnum;
import com.baomidou.mybatisplus.core.metadata.IPage;
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

    // 成功，无参数
    public static <T> Result<T> success() {
        return new Result<>(ErrorCodeEnum.SUCCESS.getCode(), null, ErrorCodeEnum.SUCCESS.getMsg());
    }

    // 成功，有参数
    public static <T> Result<T> success(T data) {
        return new Result<>(ErrorCodeEnum.SUCCESS.getCode(), data, ErrorCodeEnum.SUCCESS.getMsg());
    }

    // 成功，有参数，并转换
    public static <T, R> Result<R> success(T data, Function<T, R> function) {
        return new Result<>(ErrorCodeEnum.SUCCESS.getCode(), Objects.nonNull(data) ? function.apply(data) : null, ErrorCodeEnum.SUCCESS.getMsg());
    }

    // 成功，分页转换
    public static <T, R> Result<PageResponse<R>> success(IPage<T> page, Function<T, R> converter) {
        PageResponse<R> pageResponse = PageResponse.create(page, converter::apply);
        return new Result<>(ErrorCodeEnum.SUCCESS.getCode(), pageResponse, ErrorCodeEnum.SUCCESS.getMsg());
    }

    // 失败，无参数
    public static <T> Result<T> failure() {
        return buildFailure(ErrorCodeEnum.FAILURE.getCode(), ErrorCodeEnum.FAILURE.getMsg(), null);
    }

    // 失败，带错误消息
    public static <T> Result<T> failure(String msg) {
        return failure(ErrorCodeEnum.FAILURE, msg, null);
    }

    // 失败，带错误编码和错误消息
    public static <T> Result<T> failure(int code, String msg) {
        return buildFailure(code, msg, null);
    }

    // 失败，带错误枚举
    public static <T> Result<T> failure(ErrorCodeEnum codeEnum) {
        return failure(codeEnum, codeEnum.getMsg(), null);
    }

    // 失败，带错误枚举和自定义消息
    public static <T> Result<T> failure(ErrorCodeEnum codeEnum, String msg) {
        return failure(codeEnum, msg, null);
    }

    /**
     * 构建统一错误响应
     *
     * @param codeEnum 错误编码
     * @param msg      错误消息
     * @param data     错误详情
     * @param <T>      错误详情类型
     * @return 错误响应
     */
    public static <T> Result<T> failure(ErrorCodeEnum codeEnum, String msg, T data) {
        return buildFailure(codeEnum.getCode(), msg, data);
    }

    // 失败，带数据和错误消息
    public static <T> Result<T> failure(String msg, T data) {
        return failure(ErrorCodeEnum.FAILURE, msg, data);
    }

    // 失败，带错误编码、错误消息和数据
    public static <T> Result<T> failure(int code, String msg, T data) {
        return buildFailure(code, msg, data);
    }

    private static <T> Result<T> buildFailure(int code, String msg, T data) {
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
