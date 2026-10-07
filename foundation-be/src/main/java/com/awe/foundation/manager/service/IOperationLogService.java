package com.awe.foundation.manager.service;

import com.awe.foundation.manager.domain.operationLog.dto.req.OperationLogReq;
import com.awe.foundation.manager.domain.operationLog.entity.OperationLog;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDateTime;

/**
 * 操作审计日志服务
 */
public interface IOperationLogService extends IService<OperationLog> {

    /**
     * 异步保存审计日志
     *
     * @param operationLog 审计日志
     */
    void saveAsync(OperationLog operationLog);

    /**
     * 归档指定日期之前且未归档的日志
     *
     * @param archiveBefore 归档时间边界
     * @return 归档记录数
     */
    int archiveBefore(LocalDateTime archiveBefore);

    /**
     * 分页查询操作审计日志
     *
     * @param page 分页参数
     * @param req 查询条件
     * @return 分页日志
    */
    Page<OperationLog> page(Page<OperationLog> page, OperationLogReq req);

}
