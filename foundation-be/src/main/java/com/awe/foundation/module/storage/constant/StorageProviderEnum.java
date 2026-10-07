package com.awe.foundation.module.storage.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 云存储厂商
 */
@Getter
@AllArgsConstructor
public enum StorageProviderEnum {

    /**
     * 又拍云
     */
    UPYUN("upyun", "又拍云"),

    /**
     * S3 兼容存储
     */
    S3("s3", "S3 兼容存储");

    private final String code;
    private final String desc;
}
