package com.awe.foundation.manager.domain.operationLog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统操作审计日志实体
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_operation_log")
public class OperationLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 用户标识
     */
    private String username;
    /**
     * 日志类型
     */
    private String logType;
    /**
     * 操作名称
     */
    private String operationName;
    /**
     * 请求方法
     */
    private String requestMethod;
    /**
     * 请求路径
     */
    private String requestPath;
    /**
     * 请求IP
     */
    private String requestIp;
    /**
     * 客户端类型
     */
    private String clientType;
    /**
     * 客户端信息
     */
    private String userAgent;
    /**
     * 脱敏后的请求参数摘要
     */
    private String requestParams;
    /**
     * HTTP响应状态
     */
    private Integer responseStatus;
    /**
     * 业务结果码
     */
    private Integer resultCode;
    /**
     * 结果信息
     */
    private String resultMessage;
    /**
     * 耗时毫秒
     */
    private Long durationMs;
    /**
     * 链路追踪ID
     */
    private String traceId;
    /**
     * 异常类型
     */
    private String errorType;
    /**
     * 异常摘要
     */
    private String errorMessage;
    /**
     * 是否已归档
     */
    private Boolean archived;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 归档时间
     */
    private LocalDateTime archivedTime;

}
