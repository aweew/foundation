package com.awe.foundation.module.storage.domain.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 云存储文件查询请求
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class StorageFileReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 业务 ID
     */
    private String businessId;

    /**
     * 存储厂商编码
     */
    private String providerCode;

    /**
     * 文件状态
     */
    private String status;

    /**
     * 创建时间起点
     */
    private LocalDateTime startTime;

    /**
     * 创建时间终点
     */
    private LocalDateTime endTime;

}
