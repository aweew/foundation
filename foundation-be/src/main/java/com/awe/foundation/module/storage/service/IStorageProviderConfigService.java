package com.awe.foundation.module.storage.service;

import com.awe.foundation.module.storage.domain.dto.req.StorageProviderConfigAddReq;
import com.awe.foundation.module.storage.domain.dto.req.StorageProviderConfigUpdateReq;
import com.awe.foundation.module.storage.domain.dto.resp.StorageProviderConfigResp;
import com.awe.foundation.module.storage.domain.entity.StorageProviderConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 云存储配置服务
 */
public interface IStorageProviderConfigService extends IService<StorageProviderConfig> {

    /**
     * 查询所有未删除配置
     *
     * @return 配置列表
     */
    List<StorageProviderConfigResp> listResponses();

    /**
     * 新增配置
     *
     * @param request 新增请求
     */
    void add(StorageProviderConfigAddReq request);

    /**
     * 修改配置
     *
     * @param request 修改请求
     */
    void update(StorageProviderConfigUpdateReq request);

    /**
     * 激活配置
     *
     * @param id 配置 ID
     */
    void activate(Long id);

    /**
     * 测试配置连通性
     *
     * @param id 配置 ID
     */
    void test(Long id);

    /**
     * 删除配置
     *
     * @param id 配置 ID
     */
    void delete(Long id);

    /**
     * 查询当前默认配置
     *
     * @return 默认配置
     */
    StorageProviderConfig getActiveConfig();

}
