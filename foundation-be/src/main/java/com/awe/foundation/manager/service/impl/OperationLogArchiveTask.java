package com.awe.foundation.manager.service.impl;

import com.awe.foundation.manager.service.IOperationLogService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 操作日志归档任务
 */
@Component
public class OperationLogArchiveTask {

    @Resource
    private IOperationLogService operationLogService;

    @Value("${audit-log.retention-days:180}")
    private int retentionDays;

    /**
     * 每日凌晨归档超过保留期的日志，归档记录仍保留用于审计追溯
     */
    @Scheduled(cron = "0 30 2 * * *")
    public void archiveExpiredLogs() {
        operationLogService.archiveBefore(LocalDateTime.now().minusDays(retentionDays));
    }

}
