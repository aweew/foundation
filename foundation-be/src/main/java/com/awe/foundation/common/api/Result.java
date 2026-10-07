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

    /**
     * 返回无数据成功响应
     *
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success() {
        return new Result<>(ErrorCodeEnum.SUCCESS.getCode(), null, ErrorCodeEnum.SUCCESS.getMsg());
    }

    /**
     * 返回成功响应
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ErrorCodeEnum.SUCCESS.getCode(), data, ErrorCodeEnum.SUCCESS.getMsg());
    }

    /**
     * 返回转换后的成功响应
     *
     * @param data     原始数据
     * @param function 数据转换器
     * @param <T>      原始类型
     * @param <R>      响应类型
     * @return 成功响应
     */
    public static <T, R> Result<R> success(T data, Function<T, R> function) {
        return new Result<>(ErrorCodeEnum.SUCCESS.getCode(), Objects.nonNull(data) ? function.apply(data) : null, ErrorCodeEnum.SUCCESS.getMsg());
    }

    /**
     * 返回分页成功响应
     *
     * @param page      分页数据
     * @param converter 记录转换器
     * @param <T>       原始类型
     * @param <R>       响应类型
     * @return 分页成功响应
     */
    public static <T, R> Result<PageResponse<R>> success(IPage<T> page, Function<T, R> converter) {
        PageResponse<R> pageResponse = PageResponse.create(page, converter::apply);
        return new Result<>(ErrorCodeEnum.SUCCESS.getCode(), pageResponse, ErrorCodeEnum.SUCCESS.getMsg());
    }

    /**
     * 返回默认失败响应
     *
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> Result<T> failure() {
        return buildFailure(ErrorCodeEnum.FAILURE.getCode(), ErrorCodeEnum.FAILURE.getMsg(), null);
    }

    /**
     * 返回带消息的失败响应
     *
     * @param msg 错误消息
     * @param <T> 数据类型
     * @return 失败响应
     */
    public static <T> Result<T> failure(String msg) {
        return failure(ErrorCodeEnum.FAILURE, msg, null);
    }

    /**
     * 返回带错误码的失败响应
     *
     * @param code 错误码
     * @param msg  错误消息
     * @param <T>  数据类型
     * @return 失败响应
     */
    public static <T> Result<T> failure(int code, String msg) {
        return buildFailure(code, msg, null);
    }

    /**
     * 根据错误枚举返回失败响应
     *
     * @param codeEnum 错误枚举
     * @param <T>      数据类型
     * @return 失败响应
     */
    public static <T> Result<T> failure(ErrorCodeEnum codeEnum) {
        return failure(codeEnum, codeEnum.getMsg(), null);
    }

    /**
     * 根据错误枚举和消息返回失败响应
     *
     * @param codeEnum 错误枚举
     * @param msg      错误消息
     * @param <T>      数据类型
     * @return 失败响应
     */
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

    /**
     * 返回带详情的失败响应
     *
     * @param msg  错误消息
     * @param data 错误详情
     * @param <T>  详情类型
     * @return 失败响应
     */
    public static <T> Result<T> failure(String msg, T data) {
        return failure(ErrorCodeEnum.FAILURE, msg, data);
    }

    /**
     * 返回带错误码和详情的失败响应
     *
     * @param code 错误码
     * @param msg  错误消息
     * @param data 错误详情
     * @param <T>  详情类型
     * @return 失败响应
     */
    public static <T> Result<T> failure(int code, String msg, T data) {
        return buildFailure(code, msg, data);
    }

    private static <T> Result<T> buildFailure(int code, String msg, T data) {
        return new Result<>(code, data, msg);
    }

    /**
     * 判断响应是否失败
     *
     * @return 是否失败
     */
    @JsonIgnore
    public Boolean isError() {
        return !isSuccess();
    }

    /**
     * 判断响应是否成功
     *
     * @return 是否成功
     */
    @JsonIgnore
    public Boolean isSuccess() {
        return Objects.equals(ErrorCodeEnum.SUCCESS.getCode(), this.getCode());
    }

}
