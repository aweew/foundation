package com.awe.foundation.module.storage.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * S3 访问凭证
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class S3CredentialDTO {

    /**
     * 访问密钥标识
     */
    private String accessKey;

    /**
     * 访问密钥
     */
    private String secretAccessKey;

}
