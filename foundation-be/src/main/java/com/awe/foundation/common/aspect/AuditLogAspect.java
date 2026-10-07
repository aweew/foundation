package com.awe.foundation.common.aspect;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.awe.foundation.common.annotation.AuditLog;
import com.awe.foundation.common.api.Result;
import com.awe.foundation.common.constant.Constants;
import com.awe.foundation.common.constant.ErrorCodeEnum;
import com.awe.foundation.common.util.JsonUtils;
import com.awe.foundation.common.util.StringUtils;
import com.awe.foundation.manager.domain.operationLog.entity.OperationLog;
import com.awe.foundation.manager.service.IOperationLogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 审计注解切面
 */
@Aspect
@Component
public class AuditLogAspect {

    private static final Set<String> SENSITIVE_PROPERTIES = new HashSet<>(Arrays.asList(
            "password", "oldpassword", "newpassword", "confirmpassword", "token", "accesstoken",
            "refreshtoken", "authorization", "secret", "phone", "mobile", "idcard", "cookie"));

    @Resource
    private IOperationLogService operationLogService;

    @Resource
    private HttpServletRequest request;

    @Resource
    private HttpServletResponse response;

    /**
     * 执行标注审计注解的方法并异步写入审计日志
     */
    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        long startTime = System.currentTimeMillis();
        Long currentUserId = resolveUserId();
        Throwable failure = null;
        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable throwable) {
            failure = throwable;
            throw throwable;
        } finally {
            OperationLog.OperationLogBuilder builder = OperationLog.builder()
                    .logType(auditLog.logType())
                    .operationName(resolveOperationName(joinPoint, auditLog))
                    .requestMethod(request.getMethod()).requestPath(request.getRequestURI())
                    .requestIp(request.getRemoteAddr()).clientType(resolveClientType())
                    .userAgent(StrUtil.sub(request.getHeader("User-Agent"), 0, 512))
                    .requestParams(maskArguments(joinPoint.getArgs()))
                    .responseStatus(response.getStatus()).durationMs(System.currentTimeMillis() - startTime)
                    .traceId(MDC.get(Constants.TRACE_ID));
            Long userId = resolveUserId();
            if (userId == null) {
                userId = currentUserId;
            }
            if (userId != null) {
                builder.userId(userId);
            }
            if (failure != null) {
                builder.resultCode(ErrorCodeEnum.ERROR.getCode()).resultMessage(ErrorCodeEnum.ERROR.getMsg())
                        .errorType(failure.getClass().getName()).errorMessage(StrUtil.sub(failure.getMessage(), 0, 1000));
            } else if (result instanceof Result<?> apiResult) {
                builder.resultCode(apiResult.getCode()).resultMessage(apiResult.getMsg());
            } else {
                builder.resultCode(response.getStatus() >= 400 ? response.getStatus() : 0)
                        .resultMessage(response.getStatus() >= 400 ? "失败" : "成功");
            }
            operationLogService.saveAsync(builder.build());
        }
    }

    /**
     * 解析操作名称
     */
    private String resolveOperationName(ProceedingJoinPoint joinPoint, AuditLog auditLog) {
        if (StringUtils.isNotBlank(auditLog.operationName())) {
            return auditLog.operationName();
        }
        return ((MethodSignature) joinPoint.getSignature()).getMethod().getName();
    }

    /**
     * 记录脱敏后的方法参数
     */
    private String maskArguments(Object[] arguments) {
        Map<String, Object> values = new HashMap<>();
        for (int index = 0; index < arguments.length; index++) {
            Object argument = arguments[index];
            if (!(argument instanceof HttpServletRequest) && !(argument instanceof HttpServletResponse)) {
                values.put("arg" + index, maskValue(argument));
            }
        }
        return values.isEmpty() ? null : JsonUtils.toJsonString(values);
    }

    /**
     * 递归脱敏参数对象
     */
    private Object maskValue(Object value) {
        if (value == null || value.getClass().isPrimitive() || value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value;
        }
        if (value instanceof Iterable<?> iterable) {
            List<Object> maskedItems = new ArrayList<>();
            iterable.forEach(item -> maskedItems.add(maskValue(item)));
            return maskedItems;
        }
        try {
            Map<?, ?> map = JsonUtils.parseMap(JsonUtils.toJsonString(value));
            if (map == null) {
                return ErrorCodeEnum.REQUEST_BODY_NOT_LOGGED.getMsg();
            }
            Map<String, Object> masked = new HashMap<>();
            map.forEach((key, item) -> masked.put(String.valueOf(key), isSensitive(String.valueOf(key)) ? "***" : maskValue(item)));
            return masked;
        } catch (Exception exception) {
            return ErrorCodeEnum.REQUEST_BODY_NOT_LOGGED.getMsg();
        }
    }

    /**
     * 判断敏感字段
     */
    private boolean isSensitive(String fieldName) {
        return SENSITIVE_PROPERTIES.contains(fieldName.toLowerCase(Locale.ROOT));
    }

    /**
     * 识别客户端类型
     */
    private String resolveClientType() {
        String clientType = request.getHeader("X-Client-Type");
        return StringUtils.isBlank(clientType) ? "WEB" : StrUtil.sub(clientType, 0, 64);
    }

    /**
     * 获取当前登录用户ID
     */
    private Long resolveUserId() {
        try {
            return StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
        } catch (Exception exception) {
            return null;
        }
    }

}
