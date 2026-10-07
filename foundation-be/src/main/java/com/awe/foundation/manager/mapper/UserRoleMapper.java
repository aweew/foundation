package com.awe.foundation.manager.mapper;

import com.awe.foundation.manager.domain.userRole.entity.UserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统用户角色数据库访问层
 *
 * @author Awe
 * @since 2025-12-11 16:45:02
 */
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 查询包含已删除记录的用户角色关联
     *
     * @param userId 用户 ID
     * @param roleId 角色 ID
     * @return 用户角色关联
     */
    UserRole selectAny(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 恢复用户角色关联
     *
     * @param id 关联 ID
     * @return 更新行数
     */
    int restore(@Param("id") Long id);

}
