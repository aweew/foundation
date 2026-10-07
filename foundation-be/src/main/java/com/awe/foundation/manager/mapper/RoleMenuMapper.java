package com.awe.foundation.manager.mapper;

import com.awe.foundation.manager.domain.roleMenu.entity.RoleMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统角色权限数据库访问层
 *
 * @author Awe
 * @since 2025-12-11 16:45:00
 */
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {

    /**
     * 查询包含已删除记录的角色权限关联
     *
     * @param roleId 角色 ID
     * @param menuId 权限 ID
     * @return 角色权限关联
     */
    RoleMenu selectAny(@Param("roleId") Long roleId, @Param("menuId") Long menuId);

    /**
     * 恢复角色权限关联
     *
     * @param id 关联 ID
     * @return 更新行数
     */
    int restore(@Param("id") Long id);

}
