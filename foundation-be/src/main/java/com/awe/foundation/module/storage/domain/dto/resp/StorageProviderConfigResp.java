package com.awe.foundation.module.storage.domain.dto.resp;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 云存储配置响应
 */
@Data
public class StorageProviderConfigResp implements Serializable {

    @Serial
    private static final long serialVersionUID = 4782203141849988921L;

    /** 主键 */
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

    /** 是否已配置认证信息 */
    private Boolean credentialConfigured;

    /** 备注 */
    private String remark;

    /** 配置版本 */
    private Integer configVersion;

    /** 乐观锁版本 */
    private Integer version;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
