package com.awe.foundation.module.storage.config;

import com.awe.foundation.module.storage.config.properties.StorageProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 云存储配置
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(StorageProperties.class)
public class StorageAutoConfiguration {

}
