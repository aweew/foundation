package com.awe.foundation.common.config;

import cn.dev33.satoken.stp.StpInterface;
import com.awe.foundation.manager.service.IMenuService;
import com.awe.foundation.manager.service.IRoleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 角色和权限数据提供器
 */
@Component
@Slf4j
public class SaTokenPermissionImpl implements StpInterface {

    @Resource
    private IMenuService menuService;

    @Resource
    private IRoleService roleService;

    /**
     * 获取当前账号权限标识
     *
     * @param loginId   登录账号
     * @param loginType 登录类型
     * @return 权限标识列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        if (loginId == null) {
            return Collections.emptyList();
        }
        Long userId = Long.valueOf(loginId.toString());
        List<String> roleCodes = roleService.listCodesByUserId(userId);
        boolean superAdmin = roleCodes.stream()
                .filter(java.util.Objects::nonNull)
                .anyMatch(roleCode -> "SUPER_ADMIN".equalsIgnoreCase(roleCode.trim()));
        log.debug("加载用户角色，userId={}, roleCodes={}, superAdmin={}", userId, roleCodes, superAdmin);
        if (superAdmin) {
            return List.of("*");
        }
        return menuService.listPermissionsByUserId(userId);
    }

    /**
     * 获取当前账号角色编码
     *
     * @param loginId   登录账号
     * @param loginType 登录类型
     * @return 角色编码列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        if (loginId == null) {
            return Collections.emptyList();
        }
        return roleService.listCodesByUserId(Long.valueOf(loginId.toString()));
    }

}
