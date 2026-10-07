package com.awe.foundation.manager.service.impl;

import com.awe.foundation.common.util.StringUtils;
import com.awe.foundation.manager.domain.operationLog.dto.req.OperationLogReq;
import com.awe.foundation.manager.domain.operationLog.entity.OperationLog;
import com.awe.foundation.manager.mapper.OperationLogMapper;
import com.awe.foundation.manager.service.IOperationLogService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 操作审计日志服务实现
 */
@Service("operationLogService")
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements IOperationLogService {

    @Resource
    private OperationLogMapper operationLogMapper;

    /**
     * 异步保存审计日志
     *
     * @param operationLog 审计日志
     */
    @Override
    @Async("threadPoolTaskExecutor")
    public void saveAsync(OperationLog operationLog) {
        operationLogMapper.insert(operationLog);
    }

    /**
     * 归档指定日期之前且未归档的日志
     *
     * @param archiveBefore 归档时间边界
     * @return 归档记录数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int archiveBefore(LocalDateTime archiveBefore) {
        OperationLog update = new OperationLog();
        update.setArchived(true);
        update.setArchivedTime(LocalDateTime.now());
        return operationLogMapper.update(update, Wrappers.<OperationLog>lambdaUpdate()
                .eq(OperationLog::getArchived, false)
                .lt(OperationLog::getCreateTime, archiveBefore));
    }

    /**
     * 分页查询操作审计日志
     *
     * @param page 分页参数
     * @param req  查询条件
     * @return 分页日志
     */
    @Override
    public Page<OperationLog> page(Page<OperationLog> page, OperationLogReq req) {
        LambdaQueryWrapper<OperationLog> query = Wrappers.lambdaQuery();
        query.eq(Objects.nonNull(req.getUserId()), OperationLog::getUserId, req.getUserId())
                .eq(StringUtils.isNotBlank(req.getLogType()), OperationLog::getLogType, req.getLogType())
                .like(StringUtils.isNotBlank(req.getRequestPath()), OperationLog::getRequestPath, req.getRequestPath())
                .eq(StringUtils.isNotBlank(req.getRequestIp()), OperationLog::getRequestIp, req.getRequestIp())
                .ge(Objects.nonNull(req.getStartTime()), OperationLog::getCreateTime, req.getStartTime())
                .le(Objects.nonNull(req.getEndTime()), OperationLog::getCreateTime, req.getEndTime());
        if (!Boolean.TRUE.equals(req.getIncludeArchived())) {
            query.eq(OperationLog::getArchived, false);
        }
        query.orderByDesc(OperationLog::getCreateTime);
        return this.page(page, query);
    }

}
