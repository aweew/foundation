package com.awe.foundation.common.exception;

import com.awe.foundation.common.api.Result;
import com.awe.foundation.common.constant.ErrorCodeEnum;
import jakarta.validation.ConstraintViolationException;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * 统一异常处理器
 *
 * @author Awe
 * @since 2025/9/9 11:35
 */
public class GlobalExceptionHandler {

    // 业务异常
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        return Result.failure(e.getCode(), e.getMessage());
    }

    // 系统异常
    @ExceptionHandler(SystemException.class)
    public Result<?> handleSystemException(SystemException e) {
        return Result.failure(e.getCode(), "系统异常，请联系管理员");
    }

    // 参数校验异常（如 @Valid 抛出的）
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleValidationException(Exception e) {
        return Result.failure(ErrorCodeEnum.FAILURE.getCode(), e.getMessage());
    }

    // 兜底异常
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        return Result.failure(ErrorCodeEnum.ERROR.getCode(), "未知异常，请联系管理员");
    }

}
