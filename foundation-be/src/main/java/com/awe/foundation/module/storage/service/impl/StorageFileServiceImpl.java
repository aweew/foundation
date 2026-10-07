package com.awe.foundation.module.storage.service.impl;

import cn.hutool.core.io.file.FileNameUtil;
import com.awe.foundation.common.constant.ErrorCodeEnum;
import com.awe.foundation.common.exception.BusinessException;
import com.awe.foundation.common.util.StringUtils;
import com.awe.foundation.module.storage.config.properties.StorageProperties;
import com.awe.foundation.module.storage.domain.dto.resp.StorageFileResp;
import com.awe.foundation.module.storage.domain.dto.req.StorageFileReq;
import com.awe.foundation.module.storage.domain.entity.StorageFile;
import com.awe.foundation.module.storage.mapper.StorageFileMapper;
import com.awe.foundation.module.storage.provider.StorageProviderRouter;
import com.awe.foundation.module.storage.service.IStorageFileService;
import com.awe.foundation.module.storage.service.IStorageProviderConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * 云存储文件服务实现
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class StorageFileServiceImpl extends ServiceImpl<StorageFileMapper, StorageFile> implements IStorageFileService {

    /**
     * 分页查询文件
     *
     * @param page 分页参数
     * @param request 查询条件
     * @return 文件分页结果
     */
    @Override
    public Page<StorageFile> page(Page<StorageFile> page, StorageFileReq request) {
        return page(page, Wrappers.<StorageFile>lambdaQuery()
                .like(StringUtils.isNotBlank(request.getOriginalName()), StorageFile::getOriginalName, request.getOriginalName())
                .eq(StringUtils.isNotBlank(request.getBusinessType()), StorageFile::getBusinessType, request.getBusinessType())
                .eq(StringUtils.isNotBlank(request.getBusinessId()), StorageFile::getBusinessId, request.getBusinessId())
                .eq(StringUtils.isNotBlank(request.getProviderCode()), StorageFile::getProviderCode, request.getProviderCode())
                .eq(StringUtils.isNotBlank(request.getStatus()), StorageFile::getStatus, request.getStatus())
                .ge(Objects.nonNull(request.getStartTime()), StorageFile::getCreateTime, request.getStartTime())
                .le(Objects.nonNull(request.getEndTime()), StorageFile::getCreateTime, request.getEndTime())
                .orderByDesc(StorageFile::getCreateTime));
    }

    @Resource
    private StorageProperties storageProperties;

    @Resource
    private IStorageProviderConfigService configService;

    @Resource
    private StorageProviderRouter providerRouter;

    /**
     * 上传文件
     *
     * @param file 文件
     * @param businessType 业务类型
     * @param businessId 业务 ID
     * @return 文件信息
     */
    @Override
    public StorageFileResp upload(MultipartFile file, String businessType, String businessId) {
        if (Objects.isNull(file) || file.isEmpty()) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_UPLOAD_FAILED);
        }
        if (file.getSize() > storageProperties.getUpload().getMaxFileSize()) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_FILE_TOO_LARGE);
        }
        String contentType = file.getContentType();
        if (StringUtils.isNotBlank(contentType) && !storageProperties.getUpload().getAllowedContentTypes().isEmpty()
                && !storageProperties.getUpload().getAllowedContentTypes().contains(contentType)) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_FILE_TYPE_NOT_ALLOWED);
        }
        var config = configService.getActiveConfig();
        String extension = FileNameUtil.extName(file.getOriginalFilename());
        String objectKey = String.format("%s/%s/%s/%s%s.%s", trimPath(config.getBasePath()),
                StringUtils.isBlank(businessType) ? "common" : businessType,
                LocalDate.now().getYear(), LocalDate.now().getMonthValue(), UUID.randomUUID(),
                StringUtils.isBlank(extension) ? "bin" : extension);
        try {
            providerRouter.upload(file, objectKey);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_UPLOAD_FAILED.getMsg(), exception);
        }
        StorageFile storageFile = new StorageFile();
        storageFile.setProviderCode(config.getProviderCode());
        storageFile.setProviderConfigId(config.getId());
        storageFile.setOriginalName(file.getOriginalFilename());
        storageFile.setObjectKey(objectKey);
        storageFile.setContentType(contentType);
        storageFile.setFileSize(file.getSize());
        storageFile.setExtension(extension);
        storageFile.setBusinessType(businessType);
        storageFile.setBusinessId(businessId);
        storageFile.setStatus("SUCCESS");
        save(storageFile);
        return toResponse(storageFile, config);
    }

    /**
     * 获取文件访问地址
     *
     * @param id 文件 ID
     * @return 文件信息
     */
    @Override
    public StorageFileResp accessUrl(Long id) {
        StorageFile file = getById(id);
        if (Objects.isNull(file)) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_FILE_NOT_FOUND);
        }
        var config = configService.getById(file.getProviderConfigId());
        if (Objects.isNull(config)) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_NOT_FOUND);
        }
        return toResponse(file, config);
    }

    /**
     * 删除文件
     *
     * @param id 文件 ID
     */
    @Override
    public void delete(Long id) {
        StorageFile file = getById(id);
        if (Objects.isNull(file)) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_FILE_NOT_FOUND);
        }
        var config = configService.getById(file.getProviderConfigId());
        if (Objects.isNull(config)) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_NOT_FOUND);
        }
        providerRouter.delete(config, file.getObjectKey());
        removeById(id);
    }

    private StorageFileResp toResponse(StorageFile file, com.awe.foundation.module.storage.domain.entity.StorageProviderConfig config) {
        StorageFileResp response = new StorageFileResp();
        response.setId(file.getId());
        response.setOriginalName(file.getOriginalName());
        response.setObjectKey(file.getObjectKey());
        response.setProviderCode(file.getProviderCode());
        response.setContentType(file.getContentType());
        response.setFileSize(file.getFileSize());
        response.setAccessUrl(providerRouter.accessUrl(config, file.getObjectKey()));
        response.setExtension(file.getExtension());
        response.setBusinessType(file.getBusinessType());
        response.setBusinessId(file.getBusinessId());
        response.setStatus(file.getStatus());
        response.setCreateTime(file.getCreateTime());
        return response;
    }

    private String trimPath(String path) {
        return StringUtils.isBlank(path) ? "foundation" : path.replaceAll("^/+|/+$", "");
    }
}
