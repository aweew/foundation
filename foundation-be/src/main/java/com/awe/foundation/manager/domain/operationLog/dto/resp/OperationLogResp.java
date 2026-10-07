package com.awe.foundation.manager.domain.operationLog.dto.resp;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作审计日志响应
 */
@Data
public class OperationLogResp {

    /**
     * 主键ID
     */
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
     * 脱敏参数摘要
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
