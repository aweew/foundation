package com.awe.foundation.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要写入操作审计日志的请求方法
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /**
     * 日志类型
     */
    String logType() default "OPERATION";

    /**
     * 操作名称
     */
    String operationName() default "";

}
