package com.awe.foundation.manager.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.awe.foundation.common.api.PageResponse;
import com.awe.foundation.common.api.Result;
import com.awe.foundation.manager.domain.operationLog.dto.req.OperationLogReq;
import com.awe.foundation.manager.domain.operationLog.dto.resp.OperationLogResp;
import com.awe.foundation.manager.domain.operationLog.entity.OperationLog;
import com.awe.foundation.manager.service.IOperationLogService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作审计日志查询接口
 */
@RestController
@RequestMapping("/sys/operation-log")
public class OperationLogController {

    @Resource
    private IOperationLogService operationLogService;

    /**
     * 分页查询操作审计日志
     *
     * @param page 分页参数
     * @param req 查询条件
     * @return 分页日志
     */
    @GetMapping("/page")
    @SaCheckPermission("sys:operation-log:list")
    public Result<PageResponse<OperationLogResp>> page(Page<OperationLog> page, OperationLogReq req) {
        Page<OperationLog> result = operationLogService.page(page, req);
        PageResponse<OperationLogResp> pageResponse = PageResponse.create(result, operationLog -> {
            OperationLogResp item = new OperationLogResp();
            BeanUtils.copyProperties(operationLog, item);
            return item;
        });
        return Result.success(pageResponse);
    }
}
