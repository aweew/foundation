package com.awe.foundation.module.storage.provider.s3;

import com.awe.foundation.common.constant.ErrorCodeEnum;
import com.awe.foundation.common.exception.BusinessException;
import com.awe.foundation.common.util.JsonUtils;
import com.awe.foundation.common.util.StringUtils;
import com.awe.foundation.module.storage.domain.dto.S3CredentialDTO;
import com.awe.foundation.module.storage.domain.entity.StorageProviderConfig;
import com.awe.foundation.module.storage.provider.StorageProvider;
import com.awe.foundation.module.storage.service.StorageCredentialCipher;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;

/**
 * S3 兼容存储适配器
 */
@Component
public class S3StorageProvider implements StorageProvider {

    @Resource
    private StorageCredentialCipher credentialCipher;

    /**
     * 返回 S3 厂商编码
     *
     * @return 厂商编码
     */
    @Override
    public String providerCode() {
        return "s3";

    }

    /**
     * 上传文件到 S3 空间
     *
     * @param config    配置
     * @param file      文件
     * @param objectKey 对象路径
     * @throws IOException 文件读取失败
     */
    @Override
    public void upload(StorageProviderConfig config, MultipartFile file, String objectKey) throws IOException {
        try (S3Client client = buildClient(config); InputStream fileStream = file.getInputStream()) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(config.getServiceName()).key(objectKey.replaceFirst("^/+", ""))
                    .contentType(StringUtils.isBlank(file.getContentType()) ? "application/octet-stream" : file.getContentType())
                    .build();
            client.putObject(request, RequestBody.fromInputStream(fileStream, file.getSize()));
        } catch (Exception exception) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_UPLOAD_FAILED.getMsg(), exception);
        }

    }

    /**
     * 删除 S3 对象
     *
     * @param config    配置
     * @param objectKey 对象路径
     */
    @Override
    public void delete(StorageProviderConfig config, String objectKey) {
        try (S3Client client = buildClient(config)) {
            client.deleteObject(request -> request.bucket(config.getServiceName()).key(objectKey.replaceFirst("^/+", "")));
        } catch (Exception exception) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_DELETE_FAILED.getMsg(), exception);
        }

    }

    /**
     * 获取公开地址或有效期 15 分钟的私有对象地址
     *
     * @param config    配置
     * @param objectKey 对象路径
     * @return 对象访问地址
     */
    @Override
    public String accessUrl(StorageProviderConfig config, String objectKey) {
        String normalizedKey = objectKey.replaceFirst("^/+", "");
        if (!Boolean.TRUE.equals(config.getPrivateBucket())) {
            if (StringUtils.isBlank(config.getAccessDomain())) {
                throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_INVALID);
            }
            return config.getAccessDomain().replaceAll("/+$", "") + "/" + UriUtils.encodePath(normalizedKey, StandardCharsets.UTF_8);
        }
        try (S3Presigner presigner = S3Presigner.builder()
                .endpointOverride(URI.create(config.getEndpoint())).region(Region.of(config.getRegion()))
                .credentialsProvider(buildCredentials(config))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build()).build()) {
            GetObjectRequest request = GetObjectRequest.builder().bucket(config.getServiceName()).key(normalizedKey).build();
            return presigner.presignGetObject(GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(15)).getObjectRequest(request).build()).url().toString();
        } catch (Exception exception) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_INVALID.getMsg(), exception);
        }

    }

    /**
     * 校验 S3 空间读取权限
     *
     * @param config 配置
     */
    @Override
    public void test(StorageProviderConfig config) {
        try (S3Client client = buildClient(config)) {
            client.headBucket(request -> request.bucket(config.getServiceName()));
        } catch (Exception exception) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_TEST_FAILED.getMsg(), exception);
        }

    }

    /**
     * 构建采用路径寻址的 S3 客户端
     *
     * @param config 配置
     * @return S3 客户端
     */
    private S3Client buildClient(StorageProviderConfig config) {
        return S3Client.builder().endpointOverride(URI.create(config.getEndpoint()))
                .region(Region.of(config.getRegion())).credentialsProvider(buildCredentials(config))
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).chunkedEncodingEnabled(false).build())
                .overrideConfiguration(ClientOverrideConfiguration.builder()
                        .apiCallTimeout(Duration.ofSeconds(30)).apiCallAttemptTimeout(Duration.ofSeconds(15)).build())
                .build();

    }

    /**
     * 解密 S3 访问凭证
     *
     * @param config 配置
     * @return SDK 认证提供器
     */
    private StaticCredentialsProvider buildCredentials(StorageProviderConfig config) {
        S3CredentialDTO credential = JsonUtils.parseObject(credentialCipher.decrypt(config.getCredentialConfig()), S3CredentialDTO.class);
        if (Objects.isNull(credential) || StringUtils.isBlank(credential.getAccessKey()) || StringUtils.isBlank(credential.getSecretAccessKey())) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_INVALID);
        }
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(credential.getAccessKey(), credential.getSecretAccessKey()));

    }

}
