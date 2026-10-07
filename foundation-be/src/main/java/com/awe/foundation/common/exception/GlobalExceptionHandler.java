package com.awe.foundation.common.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.awe.foundation.common.api.Result;
import com.awe.foundation.common.constant.ErrorCodeEnum;
import com.awe.foundation.manager.domain.operationLog.entity.OperationLog;
import com.awe.foundation.manager.service.IOperationLogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 统一异常处理器
 *
 * @author Awe
 * @since 2025/9/9 11:35
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Resource
    private IOperationLogService operationLogService;

    @Resource
    private HttpServletRequest request;

    private void auditException(Exception exception) {
        operationLogService.saveAsync(OperationLog.builder().logType("EXCEPTION")
                .operationName("请求异常").requestMethod(request.getMethod()).requestPath(request.getRequestURI())
                .requestIp(request.getRemoteAddr()).errorType(exception.getClass().getName())
                .errorMessage(exception.getMessage()).responseStatus(500)
                .resultCode(ErrorCodeEnum.ERROR.getCode()).resultMessage(ErrorCodeEnum.ERROR.getMsg()).build());
    }

    // 业务异常
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        auditException(e);
        Integer code = e.getCode();
        if (Objects.isNull(code)) {
            return Result.failure(ErrorCodeEnum.FAILURE);
        }
        return Result.failure(code, e.getMessage());
    }

    // 系统异常
    @ExceptionHandler(SystemException.class)
    public Result<?> handleSystemException(SystemException e) {
        auditException(e);
        return Result.failure(ErrorCodeEnum.SYSTEM_ERROR);
    }

    /**
     * 处理未登录异常
     *
     * @param e 未登录异常
     * @return 未登录响应
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<?> handleNotLoginException(NotLoginException e) {
        auditException(e);
        return Result.failure(ErrorCodeEnum.NOT_LOGIN);
    }

    /**
     * 处理无权限异常
     *
     * @param e 无权限异常
     * @return 无权限响应
     */
    @ExceptionHandler(NotPermissionException.class)
    public Result<?> handleNotPermissionException(NotPermissionException e) {
        auditException(e);
        return Result.failure(ErrorCodeEnum.NO_PERMISSION);
    }

    // 参数校验异常（如 @Valid 抛出的）
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleValidationException(ConstraintViolationException e) {
        return Result.failure(ErrorCodeEnum.PARAMETER_ERROR);
    }

    // 参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> errors = bindingResult.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (msg1, msg2) -> msg1 // 字段重复时取第一个
                ));

        return Result.failure(ErrorCodeEnum.PARAMETER_ERROR.getCode(), ErrorCodeEnum.PARAMETER_ERROR.getMsg(), errors);
    }

    // 参数校验异常（如 @Valid 抛出的）
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        return Result.failure(ErrorCodeEnum.PARAMETER_ERROR);
    }

    // 兜底异常
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        auditException(e);
        log.error("系统异常：", e);
        return Result.failure(ErrorCodeEnum.ERROR);
    }

}
