package com.awe.foundation.module.storage.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 云存储文件元数据
 */
@Data
@TableName("storage_file")
public class StorageFile implements Serializable {

    @Serial
    private static final long serialVersionUID = -7184117308552807315L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 厂商编码 */
    private String providerCode;

    /** 厂商配置 ID */
    private Long providerConfigId;

    /** 原始文件名 */
    private String originalName;

    /** 对象路径 */
    private String objectKey;

    /** 文件类型 */
    private String contentType;

    /** 文件大小 */
    private Long fileSize;

    /** 文件扩展名 */
    private String extension;

    /** 业务类型 */
    private String businessType;

    /** 业务 ID */
    private String businessId;

    /** 文件状态 */
    private String status;

    /** 创建人 */
    private Long createUserId;

    /** 更新人 */
    private Long updateUserId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 逻辑删除 */
    private Boolean isDelete;
}
