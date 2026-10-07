package com.awe.foundation.module.storage.provider.upyun;

import com.UpYun;
import com.awe.foundation.common.constant.ErrorCodeEnum;
import com.awe.foundation.common.exception.BusinessException;
import com.awe.foundation.common.util.JsonUtils;
import com.awe.foundation.common.util.StringUtils;
import com.awe.foundation.module.storage.domain.entity.StorageProviderConfig;
import com.awe.foundation.module.storage.provider.StorageProvider;
import com.awe.foundation.module.storage.service.StorageCredentialCipher;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;

/**
 * 又拍云存储适配器
 */
@Component
public class UpyunStorageProvider implements StorageProvider {

    @Resource
    private StorageCredentialCipher credentialCipher;

    /**
     * 返回厂商编码
     *
     * @return 厂商编码
     */
    @Override
    public String providerCode() {
        return "upyun";
    }

    /**
     * 上传文件到又拍云
     *
     * @param config    配置
     * @param file      文件
     * @param objectKey 对象路径
     * @throws IOException 文件读取失败
     */
    @Override
    public void upload(StorageProviderConfig config, MultipartFile file, String objectKey) throws IOException {
        try {
            UpYun upYun = buildClient(config);
            upYun.writeFile(objectKey, file.getBytes(), true);
        } catch (Exception exception) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_UPLOAD_FAILED.getMsg(), exception);
        }
    }

    /**
     * 删除又拍云对象
     *
     * @param config    配置
     * @param objectKey 对象路径
     */
    @Override
    public void delete(StorageProviderConfig config, String objectKey) {
        try {
            buildClient(config).deleteFile(objectKey, Collections.emptyMap());
        } catch (Exception exception) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_DELETE_FAILED.getMsg(), exception);
        }
    }

    /**
     * 获取又拍云访问地址
     *
     * @param config    配置
     * @param objectKey 对象路径
     * @return 访问地址
     */
    @Override
    public String accessUrl(StorageProviderConfig config, String objectKey) {
        if (StringUtils.isBlank(config.getAccessDomain())) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_INVALID);
        }
        String domain = config.getAccessDomain().replaceAll("/+$", "");
        return domain + "/" + objectKey.replaceFirst("^/+", "");
    }

    /**
     * 测试又拍云连接
     *
     * @param config 配置
     */
    @Override
    public void test(StorageProviderConfig config) {
        try {
            buildClient(config).getFileInfo("/");
        } catch (Exception exception) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_TEST_FAILED.getMsg(), exception);
        }
    }

    private UpYun buildClient(StorageProviderConfig config) {
        if (StringUtils.isBlank(config.getServiceName()) || StringUtils.isBlank(config.getCredentialConfig())) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_INVALID);
        }
        UpyunCredential credential = JsonUtils.parseObject(credentialCipher.decrypt(config.getCredentialConfig()), UpyunCredential.class);
        if (credential == null || StringUtils.isBlank(credential.getOperator()) || StringUtils.isBlank(credential.getPassword())) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_INVALID);
        }
        UpYun upYun = new UpYun(config.getServiceName(), credential.getOperator(), credential.getPassword());
        if (StringUtils.isNotBlank(config.getEndpoint())) {
            upYun.setApiDomain(config.getEndpoint());
        }
        upYun.setTimeout(15);
        return upYun;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class UpyunCredential {

        private String operator;

        private String password;

    }

}
