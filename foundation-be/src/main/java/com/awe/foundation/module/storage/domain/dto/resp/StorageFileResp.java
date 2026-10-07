package com.awe.foundation.module.storage.domain.dto.resp;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 云存储文件响应
 */
@Data
public class StorageFileResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 6181604835738730365L;

    /**
     * 文件 ID
     */
    private Long id;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 对象路径
     */
    private String objectKey;

    /**
     * 厂商编码
     */
    private String providerCode;

    /**
     * 文件类型
     */
    private String contentType;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 访问地址
     */
    private String accessUrl;

    /**
     * 文件扩展名
     */
    private String extension;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务 ID
     */
    private String businessId;

    /**
     * 文件状态
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
