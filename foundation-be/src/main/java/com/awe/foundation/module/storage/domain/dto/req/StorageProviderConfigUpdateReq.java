package com.awe.foundation.module.storage.domain.dto.req;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 修改云存储配置请求
 */
@Data
public class StorageProviderConfigUpdateReq implements Serializable {

    @Serial
    private static final long serialVersionUID = -5598109031128854626L;

    /** 主键 */
    @NotNull
    private Long id;

    /** 厂商名称 */
    private String providerName;

    /** 是否启用 */
    private Boolean enabled;

    /** 服务端点 */
    private String endpoint;

    /** S3 签名区域 */
    private String region;

    /** S3 访问密钥标识，留空表示保留原值 */
    private String accessKey;

    /** S3 访问密钥，留空表示保留原值 */
    private String secretAccessKey;

    /** 服务名或空间名 */
    private String serviceName;

    /** 访问域名 */
    private String accessDomain;

    /** 对象根路径 */
    private String basePath;

    /** 是否私有空间 */
    private Boolean privateBucket;

    /** 又拍云操作员 */
    private String operator;

    /** 又拍云操作员密码，留空表示不修改 */
    private String password;

    /** 备注 */
    private String remark;

    /** 乐观锁版本 */
    private Integer version;
}
