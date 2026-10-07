package com.awe.foundation.module.storage.service;

import com.awe.foundation.module.storage.domain.dto.req.StorageFileReq;
import com.awe.foundation.module.storage.domain.dto.resp.StorageFileResp;
import com.awe.foundation.module.storage.domain.entity.StorageFile;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * 云存储文件服务
 */
public interface IStorageFileService extends IService<StorageFile> {

    /**
     * 分页查询文件
     *
     * @param page    分页参数
     * @param request 查询条件
     * @return 文件分页结果
     */
    Page<StorageFile> page(Page<StorageFile> page, StorageFileReq request);

    /**
     * 上传文件
     *
     * @param file         文件
     * @param businessType 业务类型
     * @param businessId   业务 ID
     * @return 文件信息
     */
    StorageFileResp upload(MultipartFile file, String businessType, String businessId);

    /**
     * 获取文件访问地址
     *
     * @param id 文件 ID
     * @return 文件信息
     */
    StorageFileResp accessUrl(Long id);

    /**
     * 删除文件
     *
     * @param id 文件 ID
     */
    void delete(Long id);

}
