package com.awe.foundation.manager.domain.operationLog.dto.req;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作审计日志查询条件
 */
@Data
public class OperationLogReq {
    /** 用户ID */
    private Long userId;
    /** 日志类型 */
    private String logType;
    /** 请求路径 */
    private String requestPath;
    /** 请求IP */
    private String requestIp;
    /** 开始时间 */
    private LocalDateTime startTime;
    /** 结束时间 */
    private LocalDateTime endTime;
    /** 是否包含已归档日志 */
    private Boolean includeArchived;
}
