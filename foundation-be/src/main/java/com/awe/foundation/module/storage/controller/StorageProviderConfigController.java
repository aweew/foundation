package com.awe.foundation.module.storage.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.awe.foundation.common.api.Result;
import com.awe.foundation.module.storage.domain.dto.req.StorageProviderConfigAddReq;
import com.awe.foundation.module.storage.domain.dto.req.StorageProviderConfigUpdateReq;
import com.awe.foundation.module.storage.domain.dto.resp.StorageProviderConfigResp;
import com.awe.foundation.module.storage.domain.entity.StorageProviderConfig;
import com.awe.foundation.module.storage.provider.StorageProviderRouter;
import com.awe.foundation.module.storage.service.IStorageProviderConfigService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 云存储配置管理
 */
@RestController
@RequestMapping("/sys/storage/provider")
public class StorageProviderConfigController {

    @Resource
    private IStorageProviderConfigService configService;

    @Resource
    private StorageProviderRouter providerRouter;

    /**
     * 查询云存储配置
     *
     * @return 配置列表
     */
    @GetMapping("/list")
    @SaCheckPermission("sys:storage:list")
    public Result<List<StorageProviderConfigResp>> list() {
        return Result.success(configService.listResponses());
    }

    /**
     * 查询云存储配置详情
     *
     * @param id 配置 ID
     * @return 配置详情
     */
    @GetMapping("/{id}")
    @SaCheckPermission("sys:storage:view")
    public Result<StorageProviderConfigResp> getById(@PathVariable Long id) {
        StorageProviderConfig config = configService.getById(id);
        return Result.success(config == null ? null : configService.listResponses().stream()
                .filter(item -> id.equals(item.getId())).findFirst().orElse(null));
    }

    /**
     * 新增云存储配置
     *
     * @param request 新增请求
     * @return 操作结果
     */
    @PostMapping
    @SaCheckPermission("sys:storage:save")
    public Result<Void> save(@Valid @RequestBody StorageProviderConfigAddReq request) {
        configService.add(request);
        return Result.success();
    }

    /**
     * 修改云存储配置
     *
     * @param request 修改请求
     * @return 操作结果
     */
    @PutMapping
    @SaCheckPermission("sys:storage:update")
    public Result<Void> update(@Valid @RequestBody StorageProviderConfigUpdateReq request) {
        configService.update(request);
        return Result.success();
    }

    /**
     * 激活云存储配置
     *
     * @param id 配置 ID
     * @return 操作结果
     */
    @PostMapping("/{id}/activate")
    @SaCheckPermission("sys:storage:activate")
    public Result<Void> activate(@PathVariable Long id) {
        configService.activate(id);
        return Result.success();
    }

    /**
     * 测试云存储配置
     *
     * @param id 配置 ID
     * @return 操作结果
     */
    @PostMapping("/{id}/test")
    @SaCheckPermission("sys:storage:test")
    public Result<Void> test(@PathVariable Long id) {
        StorageProviderConfig config = configService.getById(id);
        if (config == null) {
            return Result.failure(com.awe.foundation.common.constant.ErrorCodeEnum.STORAGE_PROVIDER_NOT_FOUND);
        }
        configService.test(id);
        providerRouter.test(config);
        return Result.success();
    }

    /**
     * 删除云存储配置
     *
     * @param id 配置 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("sys:storage:delete")
    public Result<Void> delete(@PathVariable Long id) {
        configService.delete(id);
        return Result.success();
    }
}
