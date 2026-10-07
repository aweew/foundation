package com.awe.foundation.module.storage.mapper;

import com.awe.foundation.module.storage.domain.entity.StorageFile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 云存储文件数据访问层
 */
@Mapper
public interface StorageFileMapper extends BaseMapper<StorageFile> {
}
