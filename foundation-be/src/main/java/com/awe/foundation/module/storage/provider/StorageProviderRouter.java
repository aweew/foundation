package com.awe.foundation.module.storage.provider;

import com.awe.foundation.common.constant.ErrorCodeEnum;
import com.awe.foundation.common.exception.BusinessException;
import com.awe.foundation.module.storage.domain.entity.StorageProviderConfig;
import com.awe.foundation.module.storage.service.IStorageProviderConfigService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * 云存储厂商路由
 */
@Component
public class StorageProviderRouter {

    @Resource
    private IStorageProviderConfigService configService;

    @Resource
    private List<StorageProvider> providers;

    /**
     * 上传到当前默认厂商
     *
     * @param file 文件
     * @param objectKey 对象路径
     */
    public void upload(MultipartFile file, String objectKey) throws IOException {
        StorageProviderConfig config = configService.getActiveConfig();
        getProvider(config).upload(config, file, objectKey);
    }

    /**
     * 删除指定厂商对象
     *
     * @param config 配置
     * @param objectKey 对象路径
     */
    public void delete(StorageProviderConfig config, String objectKey) {
        getProvider(config).delete(config, objectKey);
    }

    /**
     * 生成对象访问地址
     *
     * @param config 配置
     * @param objectKey 对象路径
     * @return 访问地址
     */
    public String accessUrl(StorageProviderConfig config, String objectKey) {
        return getProvider(config).accessUrl(config, objectKey);
    }

    /**
     * 测试指定厂商
     *
     * @param config 配置
     */
    public void test(StorageProviderConfig config) {
        getProvider(config).test(config);
    }

    private StorageProvider getProvider(StorageProviderConfig config) {
        return providers.stream()
                .filter(provider -> provider.providerCode().equals(config.getProviderCode()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_NOT_FOUND));
    }
}
