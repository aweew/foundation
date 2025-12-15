package com.awe.foundation.manager.mapper;

import com.awe.foundation.manager.domain.menu.dto.resp.MenuResp;
import com.awe.foundation.manager.domain.menu.entity.Menu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统权限数据库访问层
 *
 * @author Awe
 * @since 2025-12-11 16:45:01
 */
public interface MenuMapper extends BaseMapper<Menu> {

    List<MenuResp> listAllMenusByUserId(@Param("userId") Long userId);

    List<MenuResp> treeAllMenusByUserId(@Param("userId") Long userId);

    List<MenuResp> listAllMenusByRoleId(@Param("roleId") Long roleId);

    List<MenuResp> treeAllMenusByRoleId(@Param("roleId") Long roleId);

    List<String> listPermissionsByUserId(@Param("userId") Long userId);

    List<String> listPermissionsByRoleId(@Param("roleId") Long roleId);

}
