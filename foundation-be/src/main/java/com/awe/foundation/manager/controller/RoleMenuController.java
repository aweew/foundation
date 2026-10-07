package com.awe.foundation.manager.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.awe.foundation.common.api.Result;
import com.awe.foundation.manager.domain.roleMenu.convert.RoleMenuConvert;
import com.awe.foundation.manager.domain.roleMenu.dto.req.RoleMenuAddReq;
import com.awe.foundation.manager.domain.roleMenu.dto.resp.RoleMenuResp;
import com.awe.foundation.manager.domain.roleMenu.entity.RoleMenu;
import com.awe.foundation.manager.service.IRoleMenuService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色权限关联管理
 */
@RestController
@RequestMapping("/sys/role-menu")
public class RoleMenuController {

    @Resource
    private IRoleMenuService roleMenuService;

    @Resource
    private RoleMenuConvert roleMenuConvert;

    /**
     * 查询角色权限关联
     *
     * @param roleId 角色 ID
     * @return 角色权限关联列表
     */
    @GetMapping("/role/{roleId}")
    @SaCheckPermission("sys:role-menu:list")
    public Result<List<RoleMenuResp>> listByRoleId(@PathVariable("roleId") Long roleId) {
        List<RoleMenu> roleMenus = roleMenuService.list(Wrappers.<RoleMenu>lambdaQuery()
                .eq(RoleMenu::getRoleId, roleId));
        return Result.success(roleMenus, roleMenuConvert::toRespList);
    }

    /**
     * 为角色分配权限
     *
     * @param req 角色权限关联请求
     * @return 操作结果
     */
    @PostMapping
    @SaCheckPermission("sys:role-menu:save")
    public Result<Void> assign(@Valid @RequestBody RoleMenuAddReq req) {
        roleMenuService.assign(req);
        return Result.success();
    }

    /**
     * 删除角色权限关联
     *
     * @param id 关联 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @SaCheckPermission("sys:role-menu:delete")
    public Result<Void> delete(@PathVariable("id") Long id) {
        roleMenuService.removeById(id);
        return Result.success();
    }

}
