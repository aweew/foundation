package com.awe.foundation.module.storage.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.awe.foundation.common.constant.ErrorCodeEnum;
import com.awe.foundation.common.exception.BusinessException;
import com.awe.foundation.common.service.DistributedLockService;
import com.awe.foundation.common.service.RedisService;
import com.awe.foundation.common.util.JsonUtils;
import com.awe.foundation.common.util.StringUtils;
import com.awe.foundation.module.storage.constant.StorageProviderEnum;
import com.awe.foundation.module.storage.domain.dto.S3CredentialDTO;
import com.awe.foundation.module.storage.domain.dto.req.StorageProviderConfigAddReq;
import com.awe.foundation.module.storage.domain.dto.req.StorageProviderConfigUpdateReq;
import com.awe.foundation.module.storage.domain.dto.resp.StorageProviderConfigResp;
import com.awe.foundation.module.storage.domain.entity.StorageProviderConfig;
import com.awe.foundation.module.storage.mapper.StorageProviderConfigMapper;
import com.awe.foundation.module.storage.service.IStorageProviderConfigService;
import com.awe.foundation.module.storage.service.StorageCredentialCipher;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 云存储配置服务实现
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class StorageProviderConfigServiceImpl extends ServiceImpl<StorageProviderConfigMapper, StorageProviderConfig>
        implements IStorageProviderConfigService {

    private static final String ACTIVE_CONFIG_CACHE_KEY = "storage:provider:active";
    private static final String ACTIVATE_LOCK_KEY = "storage:provider:activate-lock";

    @Resource
    private StorageCredentialCipher credentialCipher;

    @Resource
    private DistributedLockService distributedLockService;

    @Resource
    private RedisService redisService;

    /**
     * 查询所有未删除配置
     *
     * @return 配置列表
     */
    @Override
    public List<StorageProviderConfigResp> listResponses() {
        return list().stream().map(this::toResponse).toList();
    }

    /**
     * 新增配置
     *
     * @param request 新增请求
     */
    @Override
    public void add(StorageProviderConfigAddReq request) {
        validateProvider(request.getProviderCode());
        validateS3Config(request.getProviderCode(), request.getEndpoint(), request.getServiceName(), request.getRegion());
        StorageProviderConfig config = new StorageProviderConfig();
        config.setProviderCode(request.getProviderCode());
        config.setProviderName(request.getProviderName());
        config.setEnabled(!Boolean.FALSE.equals(request.getEnabled()));
        config.setIsDefault(false);
        config.setEndpoint(request.getEndpoint());
        config.setRegion(request.getRegion());
        config.setServiceName(request.getServiceName());
        config.setAccessDomain(request.getAccessDomain());
        config.setBasePath(request.getBasePath());
        config.setPrivateBucket(!Boolean.FALSE.equals(request.getPrivateBucket()));
        if (StorageProviderEnum.S3.getCode().equals(request.getProviderCode())) {
            if (StringUtils.isBlank(request.getAccessKey()) || StringUtils.isBlank(request.getSecretAccessKey())) {
                throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_INVALID);
            }
            config.setCredentialConfig(credentialCipher.encrypt(JsonUtils.toJsonString(
                    new S3CredentialDTO(request.getAccessKey(), request.getSecretAccessKey()))));
        } else {
            config.setCredentialConfig(buildCredential(request.getOperator(), request.getPassword()));
        }
        config.setConfigVersion(1);
        config.setRemark(request.getRemark());
        save(config);
    }

    /**
     * 修改配置
     *
     * @param request 修改请求
     */
    @Override
    public void update(StorageProviderConfigUpdateReq request) {
        StorageProviderConfig config = getById(request.getId());
        if (Objects.isNull(config)) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_NOT_FOUND);
        }
        validateS3Config(config.getProviderCode(), request.getEndpoint(), request.getServiceName(), request.getRegion());
        config.setProviderName(request.getProviderName());
        config.setEnabled(request.getEnabled());
        config.setEndpoint(request.getEndpoint());
        config.setRegion(request.getRegion());
        config.setServiceName(request.getServiceName());
        config.setAccessDomain(request.getAccessDomain());
        config.setBasePath(request.getBasePath());
        config.setPrivateBucket(request.getPrivateBucket());
        if (StorageProviderEnum.S3.getCode().equals(config.getProviderCode())) {
            if (StringUtils.isNotBlank(request.getAccessKey()) || StringUtils.isNotBlank(request.getSecretAccessKey())) {
                S3CredentialDTO credential = JsonUtils.parseObject(credentialCipher.decrypt(config.getCredentialConfig()), S3CredentialDTO.class);
                if (Objects.isNull(credential)) {
                    throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_INVALID);
                }
                if (StringUtils.isNotBlank(request.getAccessKey())) {
                    credential.setAccessKey(request.getAccessKey());
                }
                if (StringUtils.isNotBlank(request.getSecretAccessKey())) {
                    credential.setSecretAccessKey(request.getSecretAccessKey());
                }
                if (StringUtils.isBlank(credential.getAccessKey()) || StringUtils.isBlank(credential.getSecretAccessKey())) {
                    throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_INVALID);
                }
                config.setCredentialConfig(credentialCipher.encrypt(JsonUtils.toJsonString(credential)));
            }
        } else if (StringUtils.isNotBlank(request.getOperator()) || StringUtils.isNotBlank(request.getPassword())) {
            config.setCredentialConfig(buildCredential(request.getOperator(), request.getPassword()));
        }
        config.setConfigVersion(Objects.requireNonNullElse(config.getConfigVersion(), 0) + 1);
        config.setRemark(request.getRemark());
        if (!updateById(config)) {
            throw new BusinessException(ErrorCodeEnum.OPTIMISTIC_LOCK_CONFLICT);
        }
        redisService.delete(ACTIVE_CONFIG_CACHE_KEY);
    }

    /**
     * 激活配置
     *
     * @param id 配置 ID
     */
    @Override
    public void activate(Long id) {
        StorageProviderConfig target = getById(id);
        if (Objects.isNull(target)) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_NOT_FOUND);
        }
        if (!Boolean.TRUE.equals(target.getEnabled())) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_DISABLED);
        }
        String token = distributedLockService.tryLock(ACTIVATE_LOCK_KEY, Duration.ofSeconds(10));
        if (StringUtils.isBlank(token)) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_INVALID);
        }
        try {
            update(Wrappers.<StorageProviderConfig>lambdaUpdate().set(StorageProviderConfig::getIsDefault, false)
                    .eq(StorageProviderConfig::getIsDefault, true));
            target.setIsDefault(true);
            updateById(target);
            redisService.delete(ACTIVE_CONFIG_CACHE_KEY);
        } finally {
            distributedLockService.unlock(ACTIVATE_LOCK_KEY, token);
        }
    }

    /**
     * 测试配置连通性
     *
     * @param id 配置 ID
     */
    @Override
    public void test(Long id) {
        StorageProviderConfig config = getById(id);
        if (Objects.isNull(config)) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_NOT_FOUND);
        }
        validateProvider(config.getProviderCode());
        validateS3Config(config.getProviderCode(), config.getEndpoint(), config.getServiceName(), config.getRegion());
        if (StringUtils.isBlank(config.getServiceName())
                || StringUtils.isBlank(config.getCredentialConfig())) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_TEST_FAILED);
        }
    }

    /**
     * 删除配置
     *
     * @param id 配置 ID
     */
    @Override
    public void delete(Long id) {
        StorageProviderConfig config = getById(id);
        if (Objects.isNull(config)) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_NOT_FOUND);
        }
        if (Boolean.TRUE.equals(config.getIsDefault())) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_INVALID);
        }
        removeById(id);
        redisService.delete(ACTIVE_CONFIG_CACHE_KEY);
    }

    /**
     * 查询当前默认配置
     *
     * @return 默认配置
     */
    @Override
    public StorageProviderConfig getActiveConfig() {
        String cachedId = redisService.get(ACTIVE_CONFIG_CACHE_KEY);
        if (StringUtils.isNotBlank(cachedId)) {
            StorageProviderConfig cached = getById(Long.valueOf(cachedId));
            if (Objects.nonNull(cached) && Boolean.TRUE.equals(cached.getIsDefault()) && Boolean.TRUE.equals(cached.getEnabled())) {
                return cached;
            }
        }
        StorageProviderConfig config = baseMapper.selectOne(Wrappers.<StorageProviderConfig>lambdaQuery()
                .eq(StorageProviderConfig::getIsDefault, true)
                .eq(StorageProviderConfig::getEnabled, true), false);
        if (Objects.isNull(config)) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_NOT_FOUND);
        }
        redisService.set(ACTIVE_CONFIG_CACHE_KEY, String.valueOf(config.getId()), Duration.ofMinutes(5));
        return config;
    }

    private String buildCredential(String operator, String password) {
        if (StringUtils.isBlank(operator) || StringUtils.isBlank(password)) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_INVALID);
        }
        return credentialCipher.encrypt(JsonUtils.toJsonString(new UpyunCredential(operator, password)));
    }

    private void validateProvider(String providerCode) {
        boolean supported = CollUtil.contains(List.of(StorageProviderEnum.UPYUN.getCode(), StorageProviderEnum.S3.getCode()), providerCode);
        if (!supported) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_INVALID);
        }
    }

    private StorageProviderConfigResp toResponse(StorageProviderConfig config) {
        StorageProviderConfigResp response = new StorageProviderConfigResp();
        response.setId(config.getId());
        response.setProviderCode(config.getProviderCode());
        response.setProviderName(config.getProviderName());
        response.setEnabled(config.getEnabled());
        response.setIsDefault(config.getIsDefault());
        response.setEndpoint(config.getEndpoint());
        response.setRegion(config.getRegion());
        response.setServiceName(config.getServiceName());
        response.setAccessDomain(config.getAccessDomain());
        response.setBasePath(config.getBasePath());
        response.setPrivateBucket(config.getPrivateBucket());
        response.setCredentialConfigured(StringUtils.isNotBlank(config.getCredentialConfig()));
        response.setRemark(config.getRemark());
        response.setConfigVersion(config.getConfigVersion());
        response.setVersion(config.getVersion());
        response.setUpdateTime(config.getUpdateTime());
        return response;
    }

    /**
     * 校验 S3 空间和签名配置
     *
     * @param providerCode 厂商编码
     * @param endpoint 服务端点
     * @param serviceName 空间名
     * @param region 签名区域
     */
    private void validateS3Config(String providerCode, String endpoint, String serviceName, String region) {
        if (!StorageProviderEnum.S3.getCode().equals(providerCode)) {
            return;
        }
        if (StringUtils.isBlank(endpoint) || StringUtils.isBlank(serviceName) || StringUtils.isBlank(region)) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_INVALID);
        }
        try {
            URI endpointUri = URI.create(endpoint);
            if ((!"https".equalsIgnoreCase(endpointUri.getScheme()) && !"http".equalsIgnoreCase(endpointUri.getScheme()))
                    || StringUtils.isBlank(endpointUri.getHost()) || StringUtils.isNotBlank(endpointUri.getUserInfo())
                    || StringUtils.isNotBlank(endpointUri.getQuery()) || StringUtils.isNotBlank(endpointUri.getFragment())
                    || (StringUtils.isNotBlank(endpointUri.getPath()) && !"/".equals(endpointUri.getPath()))) {
                throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_INVALID);
            }
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(ErrorCodeEnum.STORAGE_PROVIDER_INVALID);
        }

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class UpyunCredential {

        private String operator;

        private String password;

    }
}
