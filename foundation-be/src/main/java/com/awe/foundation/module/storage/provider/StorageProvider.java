package com.awe.foundation.module.storage.provider;

import com.awe.foundation.module.storage.domain.entity.StorageProviderConfig;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 云存储厂商适配器
 */
public interface StorageProvider {

    /**
     * 返回厂商编码
     *
     * @return 厂商编码
     */
    String providerCode();

    /**
     * 上传文件
     *
     * @param config 配置
     * @param file 文件
     * @param objectKey 对象路径
     */
    void upload(StorageProviderConfig config, MultipartFile file, String objectKey) throws IOException;

    /**
     * 删除对象
     *
     * @param config 配置
     * @param objectKey 对象路径
     */
    void delete(StorageProviderConfig config, String objectKey);

    /**
     * 获取访问地址
     *
     * @param config 配置
     * @param objectKey 对象路径
     * @return 访问地址
     */
    String accessUrl(StorageProviderConfig config, String objectKey);

    /**
     * 测试连接
     *
     * @param config 配置
     */
    void test(StorageProviderConfig config);
}
