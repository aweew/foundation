package com.awe.foundation.module.storage.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 云存储运行配置
 */
@Data
@ConfigurationProperties(prefix = "foundation.storage")
public class StorageProperties {

    /**
     * 是否启用云存储
     */
    private boolean enabled = true;

    /**
     * Redis配置缓存时间，单位秒
     */
    private long configCacheSeconds = 300;

    /**
     * 上传配置
     */
    private Upload upload = new Upload();

    @Data
    public static class Upload {

        /**
         * 单文件最大字节数
         */
        private long maxFileSize = 5 * 1024 * 1024;

        /**
         * 允许的文件类型
         */
        private List<String> allowedContentTypes = new ArrayList<>();
    }
}
