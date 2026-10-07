package com.awe.foundation.module.storage.domain.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 新增云存储配置请求
 */
@Data
public class StorageProviderConfigAddReq implements Serializable {

    @Serial
    private static final long serialVersionUID = 2254399014072826790L;

    /**
     * 厂商编码
     */
    @NotBlank
    private String providerCode;

    /**
     * 厂商名称
     */
    @NotBlank
    private String providerName;

    /**
     * 是否启用
     */
    private Boolean enabled = true;

    /**
     * 服务端点
     */
    private String endpoint;

    /**
     * S3 签名区域
     */
    private String region;

    /**
     * S3 访问密钥标识
     */
    private String accessKey;

    /**
     * S3 访问密钥
     */
    private String secretAccessKey;

    /**
     * 服务名或空间名
     */
    private String serviceName;

    /**
     * 访问域名
     */
    private String accessDomain;

    /**
     * 对象根路径
     */
    private String basePath;

    /**
     * 是否私有空间
     */
    private Boolean privateBucket = true;

    /**
     * 又拍云操作员
     */
    private String operator;

    /**
     * 又拍云操作员密码
     */
    private String password;

    /**
     * 备注
     */
    private String remark;

}
