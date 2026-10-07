package com.awe.foundation.module.storage.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.awe.foundation.common.api.PageResponse;
import com.awe.foundation.common.api.PageRequest;
import com.awe.foundation.common.api.Result;
import com.awe.foundation.module.storage.domain.dto.req.StorageFileReq;
import com.awe.foundation.module.storage.domain.dto.resp.StorageFileResp;
import com.awe.foundation.module.storage.domain.entity.StorageFile;
import com.awe.foundation.module.storage.service.IStorageFileService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 文件管理接口
 */
@RestController
@RequestMapping("/sys/storage/files")
public class StorageFileManageController {

    @Resource
    private IStorageFileService storageFileService;

    /**
     * 分页查询文件
     *
     * @param pageReq 分页参数
     * @param request 查询条件
     * @return 文件分页结果
     */
    @GetMapping("/page")
    @SaCheckPermission("sys:storage:file:list")
    public Result<PageResponse<StorageFileResp>> page(@Valid PageRequest pageReq, StorageFileReq request) {
        Page<StorageFile> result = storageFileService.page(pageReq.createPage(StorageFile.class), request);
        PageResponse<StorageFileResp> response = PageResponse.create(result,
                storageFile -> storageFileService.accessUrl(storageFile.getId()));
        return Result.success(response);
    }

    /**
     * 查询文件详情
     *
     * @param id 文件 ID
     * @return 文件详情
     */
    @GetMapping("/{id}")
    @SaCheckPermission("sys:storage:file:view")
    public Result<StorageFileResp> getById(@PathVariable Long id) {
        StorageFile file = storageFileService.getById(id);
        return Objects.isNull(file) ? Result.success() : Result.success(storageFileService.accessUrl(id));
    }

    /**
     * 删除文件
     *
     * @param id 文件 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("sys:storage:file:delete")
    public Result<Void> delete(@PathVariable Long id) {
        storageFileService.delete(id);
        return Result.success();
    }

}
