package com.awe.foundation.module.storage.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.awe.foundation.common.api.Result;
import com.awe.foundation.module.storage.domain.dto.resp.StorageFileResp;
import com.awe.foundation.module.storage.service.IStorageFileService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 云存储文件接口
 */
@RestController
@RequestMapping("/storage/files")
public class StorageFileController {

    @Resource
    private IStorageFileService storageFileService;

    /**
     * 上传文件
     *
     * @param file 文件
     * @param businessType 业务类型
     * @param businessId 业务 ID
     * @return 文件信息
     */
    @PostMapping
    @SaCheckLogin
    public Result<StorageFileResp> upload(@RequestPart MultipartFile file,
                                          @RequestParam(required = false) String businessType,
                                          @RequestParam(required = false) String businessId) {
        return Result.success(storageFileService.upload(file, businessType, businessId));
    }

    /**
     * 获取文件访问地址
     *
     * @param id 文件 ID
     * @return 文件信息
     */
    @GetMapping("/{id}/access-url")
    @SaCheckLogin
    public Result<StorageFileResp> accessUrl(@PathVariable Long id) {
        return Result.success(storageFileService.accessUrl(id));
    }

    /**
     * 删除文件
     *
     * @param id 文件 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @SaCheckLogin
    public Result<Void> delete(@PathVariable Long id) {
        storageFileService.delete(id);
        return Result.success();
    }
}
