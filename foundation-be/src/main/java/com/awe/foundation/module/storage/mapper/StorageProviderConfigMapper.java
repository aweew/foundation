package com.awe.foundation.module.storage.mapper;

import com.awe.foundation.module.storage.domain.entity.StorageProviderConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 云存储配置数据访问层
 */
@Mapper
public interface StorageProviderConfigMapper extends BaseMapper<StorageProviderConfig> {

}
