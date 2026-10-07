package com.awe.foundation.module.storage.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 云存储厂商配置
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("storage_provider_config")
public class StorageProviderConfig implements Serializable {

    @Serial
    private static final long serialVersionUID = 6616354289002778318L;

    /** 主键 */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 厂商编码 */
    private String providerCode;

    /** 厂商名称 */
    private String providerName;

    /** 是否启用 */
    private Boolean enabled;

    /** 是否默认厂商 */
    private Boolean isDefault;

    /** 服务端点 */
    private String endpoint;

    /** S3 签名区域 */
    private String region;

    /** 服务名或空间名 */
    private String serviceName;

    /** 访问域名 */
    private String accessDomain;

    /** 对象根路径 */
    private String basePath;

    /** 是否私有空间 */
    private Boolean privateBucket;

    /** 加密认证配置 */
    private String credentialConfig;

    /** 加密扩展配置 */
    private String extraConfig;

    /** 配置版本 */
    private Integer configVersion;

    /** 备注 */
    private String remark;

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

    /** 乐观锁版本 */
    @Version
    private Integer version;
}
